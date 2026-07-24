#!/usr/bin/env bash
# ============================================================================
# Configure UN DOMAINE pour Urgence Santé : nginx (site + API) puis HTTPS.
#
# Usage (avec sudo) :   sudo bash deploy/setup-domaine.sh mondomaine.com
#
# - Relançable sans risque (idempotent) : relancer après la propagation DNS
#   pour activer le HTTPS si le premier passage l'a signalée manquante.
# - Suppose le site sur 127.0.0.1:8085 et l'API sur 127.0.0.1:8086
#   (voir deploy/README.md et docker-compose.yml).
# ============================================================================
set -euo pipefail

DOMAIN="${1:-}"
[[ -n "$DOMAIN" ]] || { echo "Usage : sudo bash deploy/setup-domaine.sh mondomaine.com"; exit 1; }
[[ $EUID -eq 0 ]] || { echo "Ce script doit être lancé avec sudo."; exit 1; }

IP_SERVEUR="$(hostname -I | awk '{print $1}')"

echo "== 1/3 vhost nginx pour $DOMAIN =="
cat > "/etc/nginx/sites-available/$DOMAIN" <<CONF
server {
    listen 80;
    listen [::]:80;
    server_name $DOMAIN;

    # Téléchargement APK : autoriser les gros fichiers.
    client_max_body_size 200m;

    # API et documentation -> backend.
    location /api/            { proxy_pass http://127.0.0.1:8086; proxy_set_header Host \$host; }
    location /swagger-ui/     { proxy_pass http://127.0.0.1:8086; }
    location = /swagger-ui.html { proxy_pass http://127.0.0.1:8086; }
    location /v3/api-docs     { proxy_pass http://127.0.0.1:8086; }
    location = /openapi.yaml  { proxy_pass http://127.0.0.1:8086; }
    location /actuator/health { proxy_pass http://127.0.0.1:8086; }

    # Tout le reste -> site vitrine.
    location / { proxy_pass http://127.0.0.1:8085; proxy_set_header Host \$host; }
}
CONF
ln -sf "/etc/nginx/sites-available/$DOMAIN" "/etc/nginx/sites-enabled/$DOMAIN"
nginx -t
systemctl reload nginx
echo "VHOST-OK"

echo "== 2/3 le DNS de $DOMAIN pointe-t-il ce serveur ($IP_SERVEUR) ? =="
IP_DNS="$(getent hosts "$DOMAIN" | awk '{print $1}' | head -1 || true)"
echo "  $DOMAIN -> ${IP_DNS:-(pas encore résolu)}"
if [[ "${IP_DNS:-}" != "$IP_SERVEUR" ]]; then
  echo "DNS-PAS-ENCORE-PROPAGE : créez l'enregistrement A ($DOMAIN -> $IP_SERVEUR)"
  echo "chez votre registrar, attendez quelques minutes, puis RELANCEZ ce script."
  exit 0
fi

echo "== 3/3 HTTPS (certificat gratuit Let's Encrypt, renouvelé automatiquement) =="
certbot --nginx -d "$DOMAIN" --non-interactive --agree-tos --register-unsafely-without-email --redirect
echo "HTTPS-OK : https://$DOMAIN"
