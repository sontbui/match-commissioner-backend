#  health check - because project deloy in render free tier - need check to make server live 
curl -fsS "${HEALTH_URL_PROD:HEALTH_URL_DEV} || exit 1