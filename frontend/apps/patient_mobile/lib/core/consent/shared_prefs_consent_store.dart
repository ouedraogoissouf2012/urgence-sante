import 'package:shared_preferences/shared_preferences.dart';

import 'consent_store.dart';

/// Persistance du consentement via SharedPreferences (stockage local).
class SharedPrefsConsentStore implements ConsentStore {
  const SharedPrefsConsentStore();

  static const String _key = 'accepted_terms_version';

  @override
  Future<String?> acceptedTermsVersion() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_key);
  }

  @override
  Future<void> acceptTerms(String version) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, version);
  }
}
