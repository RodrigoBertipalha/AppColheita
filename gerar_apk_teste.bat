@echo off
echo Gerando APK de teste para distribuicao na equipe...
echo.

REM Navegando ate o diretorio do projeto
cd %~dp0

echo Limpando builds anteriores e caches...
call .\gradlew clean
call .\gradlew --stop

echo Gerando APK de debug (sem assinatura, para testes de campo) com as ultimas alteracoes...
REM Usamos -x lint para pular verificações de lint e --stacktrace para detalhes em caso de falha
call .\gradlew assembleDebug -x lint --stacktrace

echo.
echo Verificando se o APK foi gerado...
if exist app\build\outputs\apk\debug\app-debug.apk (
    echo APK gerado com sucesso!
    echo.
    echo Copiando APK para a pasta Desktop...
    del /F %USERPROFILE%\Desktop\ColheitaCampo-debug.apk 2>nul
copy app\build\outputs\apk\debug\app-debug.apk %USERPROFILE%\Desktop\ColheitaCampo-debug.apk
    echo.
    echo APK copiado com sucesso!
    echo Caminho do APK: %USERPROFILE%\Desktop\ColheitaCampo-debug.apk
) else (
    echo.
    echo ERRO: APK nao foi gerado. Verifique os logs acima.
)

echo.
pause
