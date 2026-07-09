import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_error.dart';
import '../../core/session/demo_session.dart';
import 'auth_api.dart';

class AuthPage extends ConsumerStatefulWidget {
  const AuthPage({super.key});

  @override
  ConsumerState<AuthPage> createState() => _AuthPageState();
}

class _AuthPageState extends ConsumerState<AuthPage> {
  final _fullNameController = TextEditingController(text: 'Cagda Dogan');
  final _emailController = TextEditingController(text: 'cagda@example.com');
  final _passwordController = TextEditingController(text: 'supersecret');
  final _codeController = TextEditingController();
  bool _loading = false;
  String? _message;

  @override
  void dispose() {
    _fullNameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _codeController.dispose();
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

  @override
  Widget build(BuildContext context) {
    final session = ref.watch(demoSessionProvider);

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _SessionCard(session: session),
        const SizedBox(height: 16),
        TextField(
          controller: _fullNameController,
          decoration: const InputDecoration(labelText: 'Full name'),
        ),
        TextField(
          controller: _emailController,
          decoration: const InputDecoration(labelText: 'Email'),
          keyboardType: TextInputType.emailAddress,
        ),
        TextField(
          controller: _passwordController,
          decoration: const InputDecoration(labelText: 'Password'),
          obscureText: true,
        ),
        const SizedBox(height: 12),
        FilledButton.icon(
          onPressed: _loading
              ? null
              : () => _run(() async {
                  final result = await ref
                      .read(authApiProvider)
                      .register(
                        fullName: _fullNameController.text.trim(),
                        email: _emailController.text.trim(),
                        password: _passwordController.text,
                      );
                  ref
                      .read(demoSessionProvider.notifier)
                      .setRegisteredUser(
                        userId: result.userId,
                        email: result.email,
                        verified: result.verified,
                      );
                  setState(
                    () => _message =
                        'Registered user ${result.userId}. Check backend logs for code.',
                  );
                }),
          icon: const Icon(Icons.person_add_alt),
          label: const Text('Register'),
        ),
        const Divider(height: 32),
        TextField(
          controller: _codeController,
          decoration: const InputDecoration(
            labelText: 'Verification code from backend log',
          ),
          keyboardType: TextInputType.number,
          maxLength: 6,
        ),
        FilledButton.icon(
          onPressed: _loading
              ? null
              : () => _run(() async {
                  final result = await ref
                      .read(authApiProvider)
                      .verifyEmail(
                        email: _emailController.text.trim(),
                        code: _codeController.text.trim(),
                      );
                  ref
                      .read(demoSessionProvider.notifier)
                      .setVerified(
                        userId: result.userId,
                        email: result.email,
                        verified: result.verified,
                      );
                  setState(
                    () =>
                        _message = 'Email verified for user ${result.userId}.',
                  );
                }),
          icon: const Icon(Icons.verified_user_outlined),
          label: const Text('Verify Email'),
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

class _SessionCard extends StatelessWidget {
  const _SessionCard({required this.session});

  final DemoSession session;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Text(
          'Demo session\n'
          'userId: ${session.currentUserId ?? '-'}\n'
          'email: ${session.email ?? '-'}\n'
          'verified: ${session.verified}',
        ),
      ),
    );
  }
}
