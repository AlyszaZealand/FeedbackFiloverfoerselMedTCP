# I README skal I senere kunne nævne: 

# ét AI-forslag I fulgte:
## Plan 1
### TRIN 1: Server starter og klient forbinder
FileServer.start() åbner ServerSocket på port
FileClient opretter Socket forbindelse
Begge printer status-beskeder
Test: Server accepterer forbindelse, begge printer deres status
### TRIN 2: Implementér GET|filnavn
Klient sender "GET|filnavn" kommandoen
Server modtager og parser med Protocol.parseCommand()
Server printer modtaget kommando
Test: Server printer "Received command: GET|testfile.txt"
### TRIN 3: Håndtér OK og ERROR
Server tjekker File.exists()
Hvis fil eksisterer: send "OK"
Hvis ikke: send "ERROR|File not found"
Klient læser første linje og detekterer status
Test: OK for eksisterende fil, ERROR for ikke-eksisterende
### TRIN 4: Send filen som bytes
Efter OK-besked: server sender fil-indhold
Brug FileInputStream med 8KB buffer-chunks
Loop: while ((bytesRead = fileInput.read(buffer)) != -1)
Test: Alle bytes sendes, ingen data tabes
### TRIN 5: Gem filen hos klienten
Klient læser fil-bytes fra InputStream
Skriver bytes til disk med FileOutputStream
Loop: while ((bytesRead = in.read(buffer)) != -1)
Test: Lokal fil er identisk med original (samme indhold og størrelse)

# ét AI-forslag I ændrede eller afviste:
## Plan 2 
### FASE 1: Grundlæggende arkitektur
 1.1 Protocol-klasse
•
Definér konstanter for kommunikation (f.eks. GET, separator |)
•
Definer metoder til parsing af kommandoer fra bytes/strings
•
Standardisér hvordan forespørgsler og svar formateres
•
Håndter encoding/decoding af data (UTF-8 for kommandoer)
 1.2 FileServer-klasse
•
Initialisering:
◦
Acceptér port-nummer som parameter
◦
Opret en ServerSocket der lytter på porten
•
Server-loop:
◦
Løbende accept af indgående klient-forbindelser
◦
For hver forbindelse: spawn en ny tråd for at håndtere den (eller brug thread pool)
◦
Læs klientens kommando fra inputstreamen
•
Kommando-behandling:
◦
Parser GET|filnavn fra inputstream
◦
Valider filnavn (sikkerhedscheck)
◦
Åbn fil og læs indhold
◦
Send fil-data over outputstream
•
Sikkerhed:
◦
Afvis filnavne der indeholder ../ (path traversal-angreb)
◦
Valider at filen kan læses
◦
Begrænser filstier til en specifik base-directory
 1.3 FileClient-klasse
•
Initialisering:
◦
Acceptér server-adresse og port
◦
Opret en Socket-forbindelse til serveren
•
Anmodning:
◦
Send GET|filnavn kommando gennem outputstream
◦
Vent på serverens svar
•
Modtagelse:
◦
Læs fil-data fra inputstream
◦
Gem data til disk under passende filnavn
◦
Luk forbindelsen når færdig
### FASE 2: Stream-håndtering
 2.1 Inputstreams (læsning)
•
FileServer:
◦
Brug Socket.getInputStream() til at læse klientens kommandoer
◦
Brug BufferedReader eller DataInputStream for struktureret læsning
◦
Brug FileInputStream til at læse fil fra disk
•
FileClient:
◦
Brug Socket.getInputStream() til at modtage fil-data fra server
◦
Læs i chunks/buffers (f.eks. 4096 bytes) for hukommelseseffektivitet
 2.2 Outputstreams (skrivning)
•
FileServer:
◦
Brug Socket.getOutputStream() til at sende fil-data til klienten
◦
Brug FileInputStream + buffering til effektiv læsning af store filer
•
FileClient:
◦
Brug Socket.getOutputStream() til at sende GET|filnavn kommando
◦
Brug FileOutputStream til at gemme modtaget data
◦
Brug BufferedWriter eller DataOutputStream for struktureret skrivning af kommandoer
 2.3 Buffer-strategi
•
Definer en konstant buffer-størrelse (f.eks. 8192 eller 4096 bytes)
•
Læs og skriv data i loops baseret på buffer-størrelse
•
Håndler end-of-stream korrekt (return value -1 fra read)
### FASE 3: Fejlhåndtering
 3.1 FileServer fejlscenarier
•
Forbindelsesfejl:
◦
Håndter IOException når Socket.accept() fejler
◦
Log fejl og fortsæt serveren kørende
•
Filfejl:
◦
Fil ikke found → Send fejlbesked til klient (f.eks. ERROR|File not found)
◦
Adgang nægtet → Send fejlbesked
◦
Read fejl → Send fejlbesked
•
Kommandofejl:
◦
Ugyldig kommandoformat → Send fejlbesked
◦
Path traversal forsøg → Afvis med fejlbesked
•
Stream-fejl:
◦
Håndel IOException ved læsning/skrivning
◦
Sikr at streams lukkes selv ved fejl (try-with-resources)
 3.2 FileClient fejlscenarier
•
Forbindelsesfejl:
◦
Server ikke tilgængelig → Fang ConnectException, skriv brugerfejl
◦
Timeout → Håndel timeout scenarios
•
Filskrivning fejl:
◦
Destination disk fuld → Fang IOException, give brugerfejl
◦
Ingen skrive-adgang → Håndel PermissionException
•
Server svar fejl:
◦
Hvis server sender ERROR|... → Parse fejlbesked og inform bruger
•
Stream-fejl:
◦
Lukket forbindelse under overførsel → Håndel EOFException
 3.3 Ressource-cleanup
•
Try-with-resources statements:
◦
Brug try (Socket socket = ...; InputStream in = ...; OutputStream out = ...) 
◦
Sikrer automatic closing selv ved exceptions
•
Eksplicit lukking:
◦
Luk socket efter brug
◦
Luk streams ordentligt
◦
Luk FileInputStream/FileOutputStream
•
ServerSocket cleanup:
◦
Shutdown ServerSocket når server stopper
◦
Luk alle aktive client sockets
### FASE 4: Testing
 4.1 Unit Tests (for Parser/Utilities)
•
Test Protocol.parse() med gyldige og ugyldige inputs
•
Test path validation med "../" forsøg
•
Test buffer-operations
 4.2 Integration Tests
•
Test 1: Forbindelsestest
◦
Start server på localhost
◦
Klient forbinder succesfuldt
◦
Forbindelse lukkes korrekt
•
Test 2: Simpel fil-overførsel
◦
Opret testfil på server
◦
Klient anmoder GET|testfile.txt
◦
Filen modtages og matches original
•
Test 3: Ukendt fil
◦
Klient anmoder fil der ikke eksisterer
◦
Server sender fejlbesked
◦
Klient modtager fejl-info
•
Test 4: Path traversal sikkerhed
◦
Klient anmoder GET|../../../etc/passwd
◦
Server afviser med fejl
◦
Ingen unauthorized fil accessed
•
Test 5: Stor fil
◦
Test med fil > buffer-størrelse
◦
Sikr korrekt chunked transfer
•
Test 6: Fejlscenarioer
◦
Server crash under overførsel
◦
Netværk afbrydelse
◦
Udfyld disk under modtagelse
•
Test 7: Ressource-cleanup
◦
Verificer alle streams lukkes
◦
Verificer server kan acceptere nye forbindelser efter test
 4.3 Manuelle tests
•
Start server i én terminal
•
Kør klient i anden terminal
•
Verificer korrekt filmodtagelse
•
Test fejlscenarier manually
