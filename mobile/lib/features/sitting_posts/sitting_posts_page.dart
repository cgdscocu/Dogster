import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_error.dart';
import '../../core/session/demo_session.dart';
import 'sitting_post_api.dart';

class SittingPostsPage extends ConsumerStatefulWidget {
  const SittingPostsPage({super.key});

  @override
  ConsumerState<SittingPostsPage> createState() => _SittingPostsPageState();
}

class _SittingPostsPageState extends ConsumerState<SittingPostsPage> {
  final _petIdController = TextEditingController();
  final _descriptionController = TextEditingController(
    text: 'Luna icin hafta sonu bakim ariyorum.',
  );
  final _latitudeController = TextEditingController(text: '41.0082');
  final _longitudeController = TextEditingController(text: '28.9784');
  final _radiusController = TextEditingController(text: '10');
  bool _loading = false;
  String? _message;
  List<SittingPostResult> _posts = [];

  @override
  void dispose() {
    _petIdController.dispose();
    _descriptionController.dispose();
    _latitudeController.dispose();
    _longitudeController.dispose();
    _radiusController.dispose();
    super.dispose();
  }

  Future<void> _run(Future<void> Function() action) async {
    setState(() {
      _loading = true;
      _message = null;
    });
    try {
      await action();
    } catch (error) {
      setState(() => _message = apiErrorMessage(error));
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  int? _currentUserId() => ref.read(demoSessionProvider).currentUserId;

  int _petId() {
    final typed = int.tryParse(_petIdController.text);
    return typed ?? ref.read(demoSessionProvider).lastPetId!;
  }

  Future<void> _createPost() async {
    final ownerId = _currentUserId();
    final lastPetId = ref.read(demoSessionProvider).lastPetId;
    if (ownerId == null ||
        (_petIdController.text.isEmpty && lastPetId == null)) {
      setState(
        () => _message = 'Register and create a pet first, or type a petId.',
      );
      return;
    }
    await _run(() async {
      final result = await ref
          .read(sittingPostApiProvider)
          .createPost(
            ownerId: ownerId,
            petId: _petId(),
            description: _descriptionController.text.trim(),
            latitude: double.parse(_latitudeController.text),
            longitude: double.parse(_longitudeController.text),
          );
      ref.read(demoSessionProvider.notifier).selectPost(result.postId);
      setState(() => _message = 'Sitting post ${result.postId} created.');
      await _listPosts();
    });
  }

  Future<void> _listPosts() async {
    final posts = await ref.read(sittingPostApiProvider).listPosts();
    setState(() => _posts = posts);
  }

  Future<void> _listNearby() async {
    await _run(() async {
      final posts = await ref
          .read(sittingPostApiProvider)
          .listNearby(
            latitude: double.parse(_latitudeController.text),
            longitude: double.parse(_longitudeController.text),
            radiusKm: double.parse(_radiusController.text),
          );
      setState(() => _posts = posts);
    });
  }

  Future<void> _assignPost(int postId) async {
    final sitterId = _currentUserId();
    if (sitterId == null) {
      setState(() => _message = 'Register first so sitterId is available.');
      return;
    }
    await _run(() async {
      final result = await ref
          .read(sittingPostApiProvider)
          .assignPost(postId: postId, sitterId: sitterId);
      ref.read(demoSessionProvider.notifier).selectPost(result.postId);
      setState(
        () => _message = 'Post ${result.postId} assigned to user $sitterId.',
      );
      await _listPosts();
    });
  }

  @override
  Widget build(BuildContext context) {
    final session = ref.watch(demoSessionProvider);
    if (_petIdController.text.isEmpty && session.lastPetId != null) {
      _petIdController.text = session.lastPetId.toString();
    }

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Card(
          child: ListTile(
            leading: const Icon(Icons.account_circle_outlined),
            title: Text('currentUserId: ${session.currentUserId ?? '-'}'),
            subtitle: Text('selectedPostId: ${session.selectedPostId ?? '-'}'),
          ),
        ),
        TextField(
          controller: _petIdController,
          decoration: const InputDecoration(labelText: 'Pet id'),
          keyboardType: TextInputType.number,
        ),
        TextField(
          controller: _descriptionController,
          decoration: const InputDecoration(labelText: 'Description'),
          maxLines: 2,
        ),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _latitudeController,
                decoration: const InputDecoration(labelText: 'Latitude'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextField(
                controller: _longitudeController,
                decoration: const InputDecoration(labelText: 'Longitude'),
              ),
            ),
          ],
        ),
        TextField(
          controller: _radiusController,
          decoration: const InputDecoration(labelText: 'Radius km'),
          keyboardType: TextInputType.number,
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            FilledButton.icon(
              onPressed: _loading ? null : _createPost,
              icon: const Icon(Icons.add),
              label: const Text('Create'),
            ),
            OutlinedButton.icon(
              onPressed: _loading ? null : () => _run(_listPosts),
              icon: const Icon(Icons.refresh),
              label: const Text('List'),
            ),
            OutlinedButton.icon(
              onPressed: _loading ? null : _listNearby,
              icon: const Icon(Icons.near_me),
              label: const Text('Nearby'),
            ),
          ],
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
        const SizedBox(height: 12),
        ..._posts.map(
          (post) => Card(
            child: ListTile(
              title: Text('#${post.postId} ${post.status}'),
              subtitle: Text(
                '${post.description}\n'
                'owner=${post.ownerId}, pet=${post.petId}, assigned=${post.assignedSitterId ?? '-'}'
                '${post.distanceKm == null ? '' : '\n${post.distanceKm!.toStringAsFixed(2)} km away'}',
              ),
              isThreeLine: true,
              onTap: () => ref
                  .read(demoSessionProvider.notifier)
                  .selectPost(post.postId),
              trailing: IconButton(
                tooltip: 'Assign',
                onPressed: post.status == 'OPEN'
                    ? () => _assignPost(post.postId)
                    : null,
                icon: const Icon(Icons.handshake_outlined),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
