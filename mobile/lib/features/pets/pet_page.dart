import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

import '../../core/api/api_error.dart';
import '../../core/session/demo_session.dart';
import 'pet_api.dart';

class PetPage extends ConsumerStatefulWidget {
  const PetPage({super.key});

  @override
  ConsumerState<PetPage> createState() => _PetPageState();
}

class _PetPageState extends ConsumerState<PetPage> {
  final _nameController = TextEditingController(text: 'Luna');
  String _type = 'DOG';
  XFile? _photo;
  bool _loading = false;
  String? _message;

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  Future<void> _pickPhoto() async {
    final picked = await ImagePicker().pickImage(source: ImageSource.gallery);
    if (picked != null) {
      setState(() => _photo = picked);
    }
  }

  Future<void> _createPet() async {
    final session = ref.read(demoSessionProvider);
    if (session.currentUserId == null) {
      setState(() => _message = 'Register first so ownerId is available.');
      return;
    }
    if (_photo == null) {
      setState(() => _message = 'Photo is required by backend.');
      return;
    }

    setState(() {
      _loading = true;
      _message = null;
    });
    try {
      final result = await ref
          .read(petApiProvider)
          .createPet(
            ownerId: session.currentUserId!,
            name: _nameController.text.trim(),
            type: _type,
            photo: _photo!,
          );
      ref.read(demoSessionProvider.notifier).setLastPetId(result.petId);
      setState(
        () => _message =
            'Pet ${result.petId} created. photoPath=${result.photoPath}',
      );
    } catch (error) {
      setState(() => _message = apiErrorMessage(error));
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final session = ref.watch(demoSessionProvider);

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Card(
          child: ListTile(
            leading: const Icon(Icons.info_outline),
            title: Text('ownerId: ${session.currentUserId ?? '-'}'),
            subtitle: Text('lastPetId: ${session.lastPetId ?? '-'}'),
          ),
        ),
        TextField(
          controller: _nameController,
          decoration: const InputDecoration(labelText: 'Pet name'),
        ),
        const SizedBox(height: 12),
        SegmentedButton<String>(
          segments: const [
            ButtonSegment(
              value: 'DOG',
              label: Text('DOG'),
              icon: Icon(Icons.pets),
            ),
            ButtonSegment(
              value: 'CAT',
              label: Text('CAT'),
              icon: Icon(Icons.cruelty_free),
            ),
          ],
          selected: {_type},
          onSelectionChanged: (values) => setState(() => _type = values.first),
        ),
        const SizedBox(height: 12),
        OutlinedButton.icon(
          onPressed: _pickPhoto,
          icon: const Icon(Icons.photo_library_outlined),
          label: Text(_photo == null ? 'Choose required photo' : _photo!.name),
        ),
        const SizedBox(height: 12),
        FilledButton.icon(
          onPressed: _loading ? null : _createPet,
          icon: const Icon(Icons.cloud_upload_outlined),
          label: const Text('Create Pet'),
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
      ],
    );
  }
}
