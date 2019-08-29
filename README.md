# Unific

Aplikacja mobilna obsługująca chat online z funkcją NFC.

Akapit wstępny:

Nasza aplikacja mobilna będzie służyć do komunikacji online w czasie rzeczywistym. 
Nawiązanie rozmowy musi być poprzedzone „zetknięciem” telefonów obu użytkowników, 
podczas którego następuje wymiana kontaktów. Jest to jedyny sposób na rozpoczęcie konwersacji z nową osobą.
Sama rozmowa może się już potem odbywać z dowolnego miejsca przez internet.

Grupa docelowa:

Użytkownicy systemu Android zainteresowani bezpiecznym oraz prostym w obsłudze komunikatorem do rozmów z przyjaciółmi.

Funkcjonalności:

- Utworzenie profilu użytkownika, dostępnego innym jedynie po wymianie kontaktów
- Dodawanie kontaktów przez NFC
- Wysyłanie wiadomości tekstowych i plików między użytkownikami w czasie rzeczywistym
- Powiadomienia związane z nowymi wiadomościami
- Przechowywanie historii konwersacji oraz ustawień użytkownika związanych z wyglądem aplikacji

Technologie:

- Aplikacja:
-- Java
-- Android
-- NFC

- Baza danych:
-- Firebase

- Narzędzia:
-- Android Studio
-- BitBucket

Harmonogram (deadliny właściwych sprintów zawsze w poniedziałki):

Dokumentacja oraz unit testy będą uzupełniane na bieżąco.

- 12 marca, godzina 23:59 (sprint 0)

- 9 kwietnia (sprint 1)
-- specyfikacja wymagań: koncepcja architektury systemu, diagram przypadków użycia, protokół bezpieczeństwa
-- szkielet aplikacji cz. 1 (activities: ekran startowy, logowanie, rejestracja, menu główne)
-- obsługa serwera cz. 1 (format drzewa z bazą danych w JSON, podstawowa komunikacja z aplikacją)
-- zakładanie kont, logowanie

- 30 kwietnia (sprint 2)
-- szkielet aplikacji cz. 2 (activities: wiadomości, kontakty, profil użytkownika, ustawienia)
-- oglądanie profili kontaktów
-- wyświetlanie wiadomości tekstowych
-- podstawowe ustawienia aplikacji
-- poprawki do specyfikacji wymagań projektu

- 14 maja (sprint 3)
-- obsługa serwera cz. 2 (autentykacja logowania, szyfrowanie wiadomości)
-- model bazy danych oraz szacowanie pojemności / workloadu

- 28 maja (sprint 4)
-- obsługa powiadomień związanych z nowo otrzymanymi wiadomościami
-- obsługa NFC
-- prezentacja, plakat

- 11 czerwca (sprint 5)
-- personalizacja wyglądu aplikacji przez użytkowników, przechowywanie tych danych na serwerze
-- wyświetlanie zdjęć w konwersacji
-- poprawki do prezentacji i plakatu

