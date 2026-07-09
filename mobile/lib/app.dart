import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'features/auth/auth_page.dart';
import 'features/messaging/messaging_page.dart';
import 'features/pets/pet_page.dart';
import 'features/sitting_posts/sitting_posts_page.dart';

class DogsterApp extends StatelessWidget {
  const DogsterApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Dogster',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF2F6F73)),
        scaffoldBackgroundColor: const Color(0xFFF7F8F6),
        useMaterial3: true,
      ),
      home: const DogsterShell(),
    );
  }
}

class DogsterShell extends ConsumerStatefulWidget {
  const DogsterShell({super.key});

  @override
  ConsumerState<DogsterShell> createState() => _DogsterShellState();
}

class _DogsterShellState extends ConsumerState<DogsterShell> {
  int _selectedIndex = 0;

  static const _pages = [
    AuthPage(),
    PetPage(),
    SittingPostsPage(),
    MessagingPage(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Dogster')),
      body: SafeArea(child: _pages[_selectedIndex]),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,
        onDestinationSelected: (index) =>
            setState(() => _selectedIndex = index),
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.person_add_alt),
            label: 'Auth',
          ),
          NavigationDestination(icon: Icon(Icons.pets), label: 'Pet'),
          NavigationDestination(icon: Icon(Icons.list_alt), label: 'Ilan'),
          NavigationDestination(
            icon: Icon(Icons.chat_bubble_outline),
            label: 'Mesaj',
          ),
        ],
      ),
    );
  }
}
