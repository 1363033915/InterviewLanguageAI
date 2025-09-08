const { createApp, reactive } = Vue

createApp({
  setup() {
    const state = reactive({ token: localStorage.getItem('token') || '', username: localStorage.getItem('username') || '' })
    const login = reactive({ username: state.username || 'admin', password: '' })

    const text = reactive({ input: '', answerHtml: '', loading: false })
    const ws = reactive({ server: `ws://${location.hostname}:8082/ws/xiaozhi/v1/`, socket: null, connected: false, message: '', logHtml: '' })
    const rec = reactive({ recording: false, wavBlob: null, status: '', loading: false, logHtml: '', ttsUrl: '' })
    const agent = reactive({ role: 'backend', level: 'mid', sessionId: '', input: '', logHtml: '' })

    function setToken(t, u) { state.token = t; localStorage.setItem('token', t); state.username = u; localStorage.setItem('username', u) }
    function clearToken() { state.token = ''; state.username = ''; localStorage.removeItem('token'); localStorage.removeItem('username') }

    async function doRegister() {
      await fetch('/api/auth/register', { method:'POST', headers:{ 'Content-Type':'application/json' }, body: JSON.stringify(login) })
      alert('注册成功或用户已存在，去登录吧')
    }
    async function doLogin() {
      const res = await fetch('/api/auth/login', { method:'POST', headers:{ 'Content-Type':'application/json' }, body: JSON.stringify(login) })
      const data = await res.json(); if (data.token) setToken(data.token, login.username)
    }
    function logout(){ clearToken(); closeWs() }

    async function sendTextChat(){
      if(!text.input.trim()) return
      text.loading = true
      try{
        const res = await fetch('/api/chat/text', { method:'POST', headers:{ 'Content-Type':'application/json', 'Authorization': 'Bearer ' + state.token }, body: JSON.stringify({ message: text.input }) })
        const data = await res.json(); text.answerHtml = `你：${escapeHtml(text.input)}\nAI：${escapeHtml(data.answer||'')}`
      } finally { text.loading = false }
    }

    function toggleWs(){ ws.connected ? closeWs() : openWs() }
    function openWs(){
      const url = new URL(ws.server)
      url.searchParams.set('device_id', 'browser')
      url.searchParams.set('token', state.token)
      ws.socket = new WebSocket(url.toString())
      ws.socket.onopen = ()=>{ ws.connected = true; logWs('WS 已连接') }
      ws.socket.onclose = ()=>{ ws.connected = false; logWs('WS 已断开') }
      ws.socket.onmessage = (ev)=>{
        if(typeof ev.data === 'string') logWs('收到文本: ' + ev.data)
        else if(ev.data instanceof Blob) { logWs('收到音频二进制'); playBlob(ev.data) }
      }
    }
    function closeWs(){ if(ws.socket){ ws.socket.close(); ws.socket=null; ws.connected=false } }
    function sendWsText(){ if(ws.socket && ws.connected){ ws.socket.send(ws.message); logWs('发送文本: '+ws.message) } }
    function logWs(t){ ws.logHtml = (ws.logHtml + (ws.logHtml? '\n':'') + t) }

    // audio record to 16k mono wav
    let media, scriptNode, audioCtx, input, buffer=[]
    async function startRecord(){
      if(rec.recording) return
      rec.status = '请求麦克风权限...'
      media = await navigator.mediaDevices.getUserMedia({ audio: true })
      audioCtx = new (window.AudioContext || window.webkitAudioContext)({ sampleRate: 16000 })
      input = audioCtx.createMediaStreamSource(media)
      scriptNode = audioCtx.createScriptProcessor(4096, 1, 1)
      scriptNode.onaudioprocess = e => {
        const ch = e.inputBuffer.getChannelData(0)
        const buf = new Float32Array(ch.length); buf.set(ch); buffer.push(buf)
      }
      input.connect(scriptNode); scriptNode.connect(audioCtx.destination)
      rec.recording = true; rec.status = '录音中...'
    }
    async function stopRecord(){
      if(!rec.recording) return
      rec.status = '处理音频...'
      scriptNode.disconnect(); input.disconnect(); media.getTracks().forEach(t=>t.stop()); await audioCtx.close()
      const pcm = floatTo16(buffer); buffer=[]
      const wav = pcm16ToWav(pcm, 16000)
      rec.wavBlob = new Blob([wav], { type: 'audio/wav' }); rec.recording = false; rec.status = '录音完成'
    }
    async function sendVoiceDialog(){
      if(!rec.wavBlob) return
      rec.loading = true
      try{
        const res = await fetch('/api/voice/dialog', { method:'POST', headers:{ 'Authorization': 'Bearer ' + state.token }, body: rec.wavBlob })
        const data = await res.json();
        rec.logHtml = `转写：${escapeHtml(data.transcript||'')}\n回复：${escapeHtml(data.answer||'')}`
        if(data.audioWavBase64){ const blob = b64ToBlob(data.audioWavBase64, 'audio/wav'); rec.ttsUrl = URL.createObjectURL(blob) }
      } finally { rec.loading = false }
    }

    function playBlob(blob){ const url = URL.createObjectURL(blob); const a = new Audio(url); a.play() }
    function escapeHtml(s){ return (s||'').replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;') }
    function floatTo16(chunks){
      const length = chunks.reduce((a,c)=>a+c.length,0)
      const out = new Int16Array(length); let off=0
      for(const c of chunks){ for(let i=0;i<c.length;i++){ let s=c[i]; s=Math.max(-1,Math.min(1,s)); out[off++]= s<0 ? s*0x8000 : s*0x7fff } }
      return out
    }
    function pcm16ToWav(pcm, sampleRate){
      const buffer = new ArrayBuffer(44 + pcm.length*2); const view = new DataView(buffer)
      let offset=0; function wstr(s){ for(let i=0;i<s.length;i++) view.setUint8(offset++, s.charCodeAt(i)) }
      function wint16(v){ view.setUint16(offset, v, true); offset+=2 }
      function wint32(v){ view.setUint32(offset, v, true); offset+=4 }
      wstr('RIFF'); wint32(36 + pcm.length*2); wstr('WAVE'); wstr('fmt '); wint32(16); wint16(1); wint16(1); wint32(sampleRate); wint32(sampleRate*2); wint16(2); wint16(16); wstr('data'); wint32(pcm.length*2)
      for(let i=0;i<pcm.length;i++) view.setInt16(offset+i*2, pcm[i], true)
      return buffer
    }
    function b64ToBlob(b64, type){ const byteChars = atob(b64); const bytes = new Uint8Array(byteChars.length); for(let i=0;i<byteChars.length;i++) bytes[i]=byteChars.charCodeAt(i); return new Blob([bytes], { type }) }

    async function startAgent(){
      const res = await fetch('/api/agent/start', { method:'POST', headers:{ 'Content-Type':'application/json', 'Authorization': 'Bearer '+state.token }, body: JSON.stringify({ role: agent.role, level: agent.level }) })
      const data = await res.json(); agent.sessionId = data.sessionId; agent.logHtml += (agent.logHtml?'\n':'') + '会话开始：' + agent.sessionId
    }
    async function sendAgent(){
      if(!agent.input.trim()) return
      const res = await fetch('/api/agent/message', { method:'POST', headers:{ 'Content-Type':'application/json', 'Authorization': 'Bearer '+state.token }, body: JSON.stringify({ sessionId: agent.sessionId, message: agent.input }) })
      const data = await res.json(); agent.logHtml += `\n你：${escapeHtml(agent.input)}\n面试官：${escapeHtml(data.answer||'')}`; agent.input=''
    }

    return { state, login, text, ws, rec, agent, doRegister, doLogin, logout, sendTextChat, toggleWs, sendWsText, startRecord, stopRecord, sendVoiceDialog, startAgent, sendAgent }
  }
}).mount('#app')


