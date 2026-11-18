# Docker backend and ngrok

This starts MySQL, the Spring Boot backend on port 4000, and an ngrok tunnel
inside Docker. The frontend remains a separately started Vite app on
`http://localhost:8443`.

## Start the services

Set your ngrok authtoken in the shell or in a root `.env` file:

```env
NGROK_AUTHTOKEN=your-ngrok-authtoken
```

Then start the stack:

```sh
docker compose up -d --build
```

Compose starts MySQL and waits for its health check before starting the
backend. Ngrok forwards to `http://backend:4000` over the Compose network.

## Set the public callback URL

Read the assigned public URL from ngrok's local API:

```sh
curl -s http://localhost:4040/api/tunnels
```

The `public_url` field for the HTTPS tunnel is the value to use. Pass it to the
backend by recreating that service with `BACKEND_PUBLIC_URL` set:

```sh
BACKEND_PUBLIC_URL=https://your-id.ngrok-free.app docker compose up -d --force-recreate backend
```

The backend uses this value to form payment provider webhook/IPN callback URLs.
For subsequent starts, put `BACKEND_PUBLIC_URL` in the root `.env` file alongside
`NGROK_AUTHTOKEN`, then run `docker compose up -d`.

The payment return URL points to `http://localhost:8443/checkout/result`, which
matches the Vite development server port. Start the frontend separately when
testing browser redirects.

Stop the stack with `docker compose down`. Add `-v` only if you also want to
remove the persisted MySQL data volume.
