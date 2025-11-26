# Script para executar a aplicação FarmaAdmin no Windows PowerShell
# Ajuste os caminhos do JDK/Maven se necessário

$env:JAVA_HOME = "C:\Users\pedroblima\.jdk\jdk-21.0.8"
$env:PATH = "C:\Users\pedroblima\.maven\maven-3.9.11\bin;$env:PATH"

Write-Host "Compilando o projeto..."
mvn clean compile

Write-Host "Executando aplicação no modo terminal (Main)..."
# Exec plugin usa a Main definida no pom.xml
mvn exec:java
