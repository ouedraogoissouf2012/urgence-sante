/// Besoin médical sélectionnable (issu du catalogue).
class MedicalNeed {
  const MedicalNeed({required this.code, required this.label});

  final String code;
  final String label;

  @override
  bool operator ==(Object other) =>
      other is MedicalNeed && other.code == code;

  @override
  int get hashCode => code.hashCode;
}
