import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';

import '../../core/api/api_error.dart';
import '../../core/session/demo_session.dart';
import 'messaging_api.dart';

class MessagingPage extends ConsumerStatefulWidget {
  const MessagingPage({super.key});

  @override
  ConsumerState<MessagingPage> createState() => _MessagingPageState();
}

class _MessagingPageState extends ConsumerState<MessagingPage> {
  final _postIdController = TextEditingController();
  final _contentController = TextEditingController(
    text: 'Merhaba, detaylari konusalim.',
  );
  final List<MessageResult> _messages = [];
  StompClient? _client;
  bool _connected = false;
  bool _loading = false;
  String? _message;

  @override
  void dispose() {
    _client?.deactivate();
    _postIdController.dispose();
    _contentController.dispose();
    super.dispose();
  }

  int? _postId() => int.tryParse(_postIdController.text);

  int? _currentUserId() => ref.read(demoSessionProvider).currentUserId;

  Future<void> _loadHistory() async {
    final postId = _postId();
    final requesterId = _currentUserId();
    if (postId == null || requesterId == null) {
      setState(() => _message = 'Select a post and register first.');
      return;
    }
    setState(() {
      _loading = true;
      _message = null;
    });
    try {
      final messages = await ref
          .read(messagingApiProvider)
          .listMessages(postId: postId, requesterId: requesterId);
      setState(() {
        _messages
          ..clear()
          ..addAll(messages);
      });
    } catch (error) {
      setState(() => _message = apiErrorMessage(error));
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  void _connect() {
    final postId = _postId();
    if (postId == null) {
      setState(() => _message = 'Type or select a postId first.');
      return;
    }

    _client?.deactivate();
    final client = StompClient(
      config: StompConfig(
        url: const String.fromEnvironment(
          'DOGSTER_WS_URL',
          defaultValue: 'ws://10.0.2.2:8080/ws',
        ),
        onConnect: (frame) {
          setState(() {
            _connected = true;
            _message = 'Connected to post $postId topic.';
          });
          _client?.subscribe(
            destination: '/topic/sitting-posts/$postId/messages',
            callback: (frame) {
              final body = frame.body;
              if (body == null) {
                return;
              }
              final decoded = jsonDecode(body) as Map<String, dynamic>;
              setState(() => _messages.add(MessageResult.fromJson(decoded)));
            },
          );
        },
        onWebSocketError: (error) =>
            setState(() => _message = error.toString()),
        onStompError: (frame) =>
            setState(() => _message = frame.body ?? 'STOMP error'),
        onDisconnect: (frame) => setState(() => _connected = false),
      ),
    );
    _client = client;
    client.activate();
  }

  void _send() {
    final postId = _postId();
    final senderId = _currentUserId();
    if (!_connected || postId == null || senderId == null) {
      setState(
        () => _message = 'Connect first and make sure session has userId.',
      );
      return;
    }
    _client?.send(
      destination: '/app/sitting-posts/$postId/messages',
      body: jsonEncode({
        'senderId': senderId,
        'content': _contentController.text.trim(),
      }),
    );
    _contentController.clear();
  }

  @override
  Widget build(BuildContext context) {
    final session = ref.watch(demoSessionProvider);
    if (_postIdController.text.isEmpty && session.selectedPostId != null) {
      _postIdController.text = session.selectedPostId.toString();
    }

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Card(
          child: ListTile(
            leading: Icon(_connected ? Icons.wifi : Icons.wifi_off),
            title: Text('userId: ${session.currentUserId ?? '-'}'),
            subtitle: Text('selectedPostId: ${session.selectedPostId ?? '-'}'),
          ),
        ),
        TextField(
          controller: _postIdController,
          decoration: const InputDecoration(labelText: 'Assigned post id'),
          keyboardType: TextInputType.number,
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            OutlinedButton.icon(
              onPressed: _loading ? null : _loadHistory,
              icon: const Icon(Icons.history),
              label: const Text('History'),
            ),
            FilledButton.icon(
              onPressed: _connect,
              icon: const Icon(Icons.cable),
              label: const Text('Connect'),
            ),
          ],
        ),
        TextField(
          controller: _contentController,
          decoration: const InputDecoration(labelText: 'Message'),
          minLines: 1,
          maxLines: 3,
        ),
        const SizedBox(height: 12),
        FilledButton.icon(
          onPressed: _send,
          icon: const Icon(Icons.send),
          label: const Text('Send'),
        ),
        if (_loading)
          const Padding(
            padding: EdgeInsets.all(12),
            child: LinearProgressIndicator(),
          ),
        if (_message != null)
          Padding(
            padding: const EdgeInsets.only(top: 12),
            child: Text(_message!),
          ),
        const Divider(height: 32),
        ..._messages.map(
          (message) => ListTile(
            leading: CircleAvatar(child: Text(message.senderId.toString())),
            title: Text(message.content),
            subtitle: Text(message.sentAt.toLocal().toString()),
          ),
        ),
      ],
    );
  }
}
