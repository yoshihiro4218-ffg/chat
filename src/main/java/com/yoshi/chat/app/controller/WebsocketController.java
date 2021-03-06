package com.yoshi.chat.app.controller;

import com.yoshi.chat.app.common.util.*;
import com.yoshi.chat.app.coordinator.*;
import com.yoshi.chat.app.properties.*;
import com.yoshi.chat.domain.entity.*;
import com.yoshi.chat.domain.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.*;

// 練習として、こちらはWebSocketを使用してみます。。。
@Controller
@AllArgsConstructor
@Slf4j
public class WebsocketController {
    private final ChatCoordinator chatCoordinator;
    private final ChatLogService chatLogService;
    private final UserService userService;
    private final ChatLogProperties chatLogProperties;
    private final CookieNameProperties cookieNameProperties;

    @GetMapping("/websocket/form")
    public String form(Model model) {
        List<ChatLog> chatLogs =
                chatLogService.findAllLimit(chatLogProperties.getLimit());
        List<ChatLogResponse> chatLogResponses =
                chatLogs.stream()
                        .map(chatLog -> {
                            String userName = userService.find(chatLog.getUserId())
                                                         .orElseThrow(() -> new RuntimeException("No User"))
                                                         .getUserName();
                            return ChatLogResponse.builder()
                                                  .chatLog(chatLog)
                                                  .userName(userName)
                                                  .build();
                        })
                        .collect(Collectors.toList());
        model.addAttribute("chatLogResponses", chatLogResponses);
        return "pages/formWebSocket";
    }

    @MessageMapping(value = "/websocket/new" /* 宛先名 */)
    // Controller内の@MessageMappingアノテーションをつけたメソッドが、メッセージを受け付ける
    @SendTo(value = "/queue/chat") // 処理結果の送り先
    @ResponseBody
    public String chat(String message) {
//        String cookieValue = RetrieveCookieUtil.retrieveValue(request.getCookies(),
//                                                              cookieNameProperties.getUserCookie());
//        chatCoordinator.confirmUserAndCreateChatLog(cookieValue, message);
        log.info("WebSocket:{}", message);
        return message;
    }

    @Value
    @Builder
    private static class ChatLogResponse {
        ChatLog chatLog;
        String userName;
    }
}
