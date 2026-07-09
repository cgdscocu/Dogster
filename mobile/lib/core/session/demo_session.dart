import 'package:flutter_riverpod/flutter_riverpod.dart';

class DemoSession {
  const DemoSession({
    this.currentUserId,
    this.email,
    this.verified = false,
    this.lastPetId,
    this.selectedPostId,
  });

  final int? currentUserId;
  final String? email;
  final bool verified;
  final int? lastPetId;
  final int? selectedPostId;

  DemoSession copyWith({
    int? currentUserId,
    String? email,
    bool? verified,
    int? lastPetId,
    int? selectedPostId,
  }) {
    return DemoSession(
      currentUserId: currentUserId ?? this.currentUserId,
      email: email ?? this.email,
      verified: verified ?? this.verified,
      lastPetId: lastPetId ?? this.lastPetId,
      selectedPostId: selectedPostId ?? this.selectedPostId,
    );
  }
}

class DemoSessionNotifier extends Notifier<DemoSession> {
  @override
  DemoSession build() => const DemoSession();

  void setRegisteredUser({
    required int userId,
    required String email,
    required bool verified,
  }) {
    state = state.copyWith(
      currentUserId: userId,
      email: email,
      verified: verified,
    );
  }

  void setVerified({
    required int userId,
    required String email,
    required bool verified,
  }) {
    state = state.copyWith(
      currentUserId: userId,
      email: email,
      verified: verified,
    );
  }

  void setLastPetId(int petId) {
    state = state.copyWith(lastPetId: petId);
  }

  void selectPost(int postId) {
    state = state.copyWith(selectedPostId: postId);
  }
}

final demoSessionProvider = NotifierProvider<DemoSessionNotifier, DemoSession>(
  DemoSessionNotifier.new,
);
