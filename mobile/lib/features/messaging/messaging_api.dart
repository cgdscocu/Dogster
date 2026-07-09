import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client.dart';

class MessageResult {
  const MessageResult({
    required this.messageId,
    required this.postId,
    required this.senderId,
    required this.content,
    required this.sentAt,
  });

  final int messageId;
  final int postId;
  final int senderId;
  final String content;
  final DateTime sentAt;

  factory MessageResult.fromJson(Map<String, dynamic> json) {
    return MessageResult(
      messageId: json['messageId'] as int,
      postId: json['postId'] as int,
      senderId: json['senderId'] as int,
      content: json['content'] as String,
      sentAt: DateTime.parse(json['sentAt'] as String),
    );
  }
}

class MessagingApi {
  MessagingApi(this._dio);

  final Dio _dio;

  Future<List<MessageResult>> listMessages({
    required int postId,
    required int requesterId,
  }) async {
    final response = await _dio.get<List<dynamic>>(
      '/api/sitting-posts/$postId/messages',
      queryParameters: {'requesterId': requesterId},
    );
    return response.data!
        .cast<Map<String, dynamic>>()
        .map(MessageResult.fromJson)
        .toList();
  }
}

final messagingApiProvider = Provider<MessagingApi>(
  (ref) => MessagingApi(ref.watch(dioProvider)),
);
