# Beleg: Programmierung verteilter Systeme

----

## Aufgabenstellung

Es soll ein verteiltes System entwickelt werden, welches die folgenden Anforderungen erfüllt:

- Berechnung einer Mandelbrot-Menge
- Darstellung mittels GUI & dynamischen Updates via MVC-Architektur
- Speichern der Berechnungsergebnisse in Bildern
- Generierung eines Videos aus den Bildern

## Inbetriebnahme

#### 1. Anlegen einer `.env` Datei im Projektverzeichnis unter `/src/` mit folgendem Inhalt:

```bash
SERVER_IP="192.168.178.xxx"
SERVER_PORT=1337

CLIENT_IP="192.168.178.xxx"
```

#### 2. Starten des `Servers`:

```bash
java ApfelServer.java
```

#### 3. Starten der `View` inkl. des `Clients`:

```bash
java ApfelView.java
```

#### 4. Generierung der Mandelbrot-Menge im GUI durch Drücken des `Mandelbrotmenge generieren`-Buttons

# Beispiel

### https://vaultwarden.counteresp.de/#/send/JUxkZyfTS92HERZRzzvw7Q/Jl0doKEYxguWEVEeo4vQgw[Mandelbrot Beispiel.mp4]
