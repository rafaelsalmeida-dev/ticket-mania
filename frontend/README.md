# Ticket Mania frontend

Frontend do Ticket Mania construído com Next.js App Router, React, TypeScript, Tailwind CSS e componentes reutilizáveis em `src/components/ui`.

## Requisitos

- Node.js compatível com Next.js 15
- Backend Ticket Mania disponível
- Dependências instaladas com `npm ci`

Configure `BACKEND_API_URL` em `frontend/.env.local` apontando para a origem do backend, por exemplo:

```env
BACKEND_API_URL=http://localhost:8080
```

Essa variável é usada exclusivamente no servidor Next.js. Não use prefixo `NEXT_PUBLIC_` nem exponha a URL/credenciais do backend ao navegador.

## Executar

```bash
npm run dev
```

O frontend fica disponível em `http://localhost:3000`.

## Funcionalidades

- `/`: busca, paginação e catálogo público de eventos.
- `/events/[id]`: detalhes, disponibilidade e seleção de ingressos.
- `/login` e `/register`: autenticação e cadastro integrados à API.
- `/orders` e `/orders/[id]`: pedidos do usuário, ingressos e confirmação do pagamento de demonstração do backend.
- `/admin/events`: criação, edição e exclusão de eventos, lotes, preços e estoque.
- `/admin/users/role`: alteração de permissões pelo UUID, conforme o endpoint disponível no backend.

O restante das chamadas autenticadas passa pelo endpoint server-side `/api/proxy/*`, que encaminha o JWT no cabeçalho Bearer sem entregá-lo ao JavaScript do navegador.

## Autenticação e segurança

O token é gravado exclusivamente no cookie `ticket_mania_token` com `HttpOnly`, `SameSite=Lax`, `Path=/` e `Secure` em produção. O cliente nunca lê o token, e não há persistência de autenticação em `localStorage` ou `sessionStorage`. O endpoint de logout expira o cookie; chamadas autenticadas que retornam 401 removem a sessão e encaminham o usuário ao login.

As rotas `/orders/*` e `/admin/*` são protegidas pelo middleware. O middleware serve somente como guarda de navegação; a autorização final continua sendo feita pelo backend. As operações de escrita nos endpoints server-side verificam a origem da requisição para proteção contra CSRF; o navegador e o backend comunicam-se por meio do proxy same-origin.

O backend do projeto usa confirmação de pagamento mock. O botão de pagamento chama somente o endpoint `PATCH /api/v1/orders/mock-success/{orderId}`; nenhum pagamento real é processado.

## Verificações

```bash
npm run typecheck
npm run lint
npm run build
```
