# Sistema de Agendamento de Consultas

API REST para agendamento de consultas, construída com **Java + Spring Boot**. Projeto de portfólio focado em regras de negócio (conflito de horários, expediente) e integração com serviços AWS.

## Funcionalidades

- **Autenticação JWT** — registro e login de usuários, rotas protegidas por token
- **Pacientes** — CRUD completo
- **Profissionais** — CRUD completo, com horário de expediente configurável
- **Agendamento de consultas** — valida dia útil, expediente do profissional e ausência de conflito de horário
- **Cancelamento de consultas**
- **Notificação por e-mail** (confirmação e cancelamento) via **AWS SES**, enviada de forma assíncrona
- **Documentação interativa** via Swagger/OpenAPI
- **Tratamento global de erros** com respostas padronizadas

## Stack

| Camada          | Tecnologia                          |
|-----------------|--------------------------------------|
| Linguagem       | Java 21                              |
| Framework       | Spring Boot 3.3.4                    |
| Segurança       | Spring Security + JWT (JJWT)         |
| Persistência    | Spring Data JPA (Hibernate)          |
| Banco de dados  | PostgreSQL 17                        |
| E-mail          | AWS SES (AWS SDK v2)                 |
| Documentação    | springdoc-openapi (Swagger UI)       |
| Build           | Maven                                |
| Containerização | Docker / Docker Compose              |

## Arquitetura

```
com.agendamento.api
├── auth           -> registro/login, geração de JWT
├── security        -> filtro JWT, geração/validação de token
├── user             -> conta de usuário (autenticação)
├── patient          -> cadastro de pacientes
├── professional      -> cadastro de profissionais e expediente
├── appointment        -> agendamento/cancelamento de consultas
├── notification         -> envio de e-mail (interface + AWS SES + fallback local)
├── config          -> Spring Security, Swagger
└── exception        -> tratamento global de erros
```

### Sobre a notificação por e-mail

O envio de e-mail é abstraído por uma interface (`EmailService`), com duas implementações:

- **`ConsoleEmailService`** (padrão): apenas registra o e-mail no log. Permite rodar e testar o projeto localmente **sem precisar de conta AWS**.
- **`SesEmailService`**: envia de verdade via AWS SES. Ativada com `NOTIFICATION_PROVIDER=ses`.

Isso é um exemplo do padrão **Strategy**: a regra de negócio (`AppointmentService`) não sabe (nem precisa saber) qual implementação está enviando o e-mail.

### Sobre a regra de conflito de horário

Cada profissional tem um expediente fixo (`workStart`–`workEnd`, de segunda a sexta — simplificação intencional, ver "Próximos passos"). Ao criar uma consulta, o sistema:

1. Rejeita finais de semana
2. Rejeita horários fora do expediente do profissional
3. Busca as consultas já marcadas do profissional naquele dia e rejeita se houver sobreposição de horário

## Como rodar localmente

### Pré-requisitos
- Java 21 (JDK)
- Maven (ou use o `mvnw` incluso)
- PostgreSQL 17 rodando localmente **ou** Docker

### Opção 1: PostgreSQL local

```bash
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="agendamento_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="sua_senha"
mvn spring-boot:run
```

O banco `agendamento_db` precisa existir previamente (diferente do MySQL, o driver do Postgres não cria o banco automaticamente). Crie com:
```bash
psql -U postgres -c "CREATE DATABASE agendamento_db;"
```

### Opção 2: Docker Compose (recomendado)

```bash
docker compose up --build
```

### Acessando a API

- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`

## Configurando o envio de e-mail via AWS SES (opcional)

Por padrão, o projeto roda com `NOTIFICATION_PROVIDER=console` (não precisa de AWS). Para usar o SES de verdade:

1. No console AWS → **SES** → verifique um e-mail remetente (Verified identities)
2. Se a conta SES estiver em modo **sandbox**, verifique também o e-mail do destinatário de teste
3. Gere uma Access Key (IAM → usuário com permissão `ses:SendEmail`) — **nunca coloque essas chaves no código**
4. Configure as variáveis de ambiente:

```bash
$env:NOTIFICATION_PROVIDER="ses"
$env:SES_FROM_EMAIL="seu-email-verificado@exemplo.com"
$env:AWS_REGION="us-east-1"
$env:AWS_ACCESS_KEY_ID="sua-access-key"
$env:AWS_SECRET_ACCESS_KEY="sua-secret-key"
```

## Fluxo de uso (exemplo)

```bash
# 1. Registrar e logar
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Ana","email":"ana@email.com","password":"senha123"}'

# 2. Cadastrar um profissional (expediente 08:00-18:00)
curl -X POST http://localhost:8081/api/professionals \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"name":"Dr. Carlos","specialty":"Clinico Geral","email":"carlos@clinica.com","workStart":"08:00","workEnd":"18:00"}'

# 3. Cadastrar um paciente
curl -X POST http://localhost:8081/api/patients \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"name":"Joao","email":"joao@email.com","phone":"11999999999"}'

# 4. Agendar consulta
curl -X POST http://localhost:8081/api/appointments \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"patientId":1,"professionalId":1,"dateTime":"2026-09-01T10:00:00","durationMinutes":30}'

# 5. Cancelar
curl -X PATCH http://localhost:8081/api/appointments/1/cancel \
  -H "Authorization: Bearer <TOKEN>"
```

## Deploy na AWS

Veja o guia completo em [`DEPLOY.md`](./DEPLOY.md) — cobre RDS (PostgreSQL), configuração do SES e deploy via Elastic Beanstalk.

## Próximos passos (ideias de evolução)

- Expediente configurável por dia da semana (hoje é fixo Seg-Sex)
- Múltiplos horários de atendimento por profissional (ex: manhã e tarde)
- Testes automatizados (JUnit/Mockito, incluindo o `EmailService` mockado)
- Fila (SQS) entre o agendamento e o envio de e-mail, para maior resiliência
- Lembrete automático de consulta (ex: 24h antes) com um job agendado
