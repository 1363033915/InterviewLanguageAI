package com.interview.chat;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatLogMapper {
    int insert(ChatLog log);
}


