# Deploy na AWS

Este guia cobre **RDS (PostgreSQL)** + **SES (e-mail)** + **Elastic Beanstalk** (roda o `.jar`).

> Criar esses recursos pode gerar custos. Use o **free tier** e **destrua os recursos** ao terminar de demonstrar o projeto.

## Pré-requisitos

- Conta AWS própria, configurada com `aws configure`
- Projeto compilando localmente (`mvn clean package`)

## Passo 1 — Configurar o AWS SES (envio de e-mail)

1. Console AWS → **SES** → **Verified identities** → **Create identity**
2. Verifique um e-mail (ex: o seu) — a AWS manda um link de confirmação
3. Enquanto a conta estiver em **modo sandbox** (padrão para contas novas), você só consegue enviar e-mails para endereços também verificados. Para enviar a qualquer destinatário, solicite "**Request production access**" no console do SES.
4. Crie um usuário IAM com a policy `AmazonSESFullAccess` (ou uma policy customizada restrita a `ses:SendEmail`) e gere uma **Access Key**

## Passo 2 — Criar o banco (RDS PostgreSQL)

1. Console AWS → **RDS** → **Create database**
2. Engine: **PostgreSQL** (versão 17)
3. Template: **Free tier**
4. DB instance identifier: `agendamento-db`
5. Master username/password de sua escolha
6. Connectivity → **Public access: Yes** (para simplificar; em produção real, use uma VPC privada)
7. Depois de criado, conecte e crie o banco:
   ```bash
   psql -h <endpoint-do-rds> -U <usuario> -c "CREATE DATABASE agendamento_db;"
   ```

## Passo 3 — Gerar o pacote da aplicação

```bash
mvn clean package -DskipTests
```

## Passo 4 — Criar o ambiente no Elastic Beanstalk

1. Console AWS → **Elastic Beanstalk** → **Create application**
2. Platform: **Java** (Corretto 21)
3. Upload do `.jar` gerado
4. Em **Environment properties**, defina:

   | Nome                     | Valor                                 |
   |--------------------------|----------------------------------------|
   | `DB_HOST`                | endpoint do RDS                        |
   | `DB_PORT`                | `5432`                                  |
   | `DB_NAME`                | `agendamento_db`                        |
   | `DB_USERNAME`            | usuário do RDS                          |
   | `DB_PASSWORD`            | senha do RDS                            |
   | `JWT_SECRET`             | uma string base64 aleatória nova        |
   | `NOTIFICATION_PROVIDER`  | `ses`                                    |
   | `SES_FROM_EMAIL`         | e-mail verificado no SES                |
   | `AWS_REGION`             | região onde o SES/RDS foram criados     |
   | `AWS_ACCESS_KEY_ID`      | access key do usuário IAM               |
   | `AWS_SECRET_ACCESS_KEY`  | secret key do usuário IAM               |
   | `SERVER_PORT`            | `5000`                                   |

   > Em um cenário mais realista, `AWS_ACCESS_KEY_ID`/`SECRET` não seriam necessários aqui: você anexaria uma **IAM Role** diretamente à instância do Elastic Beanstalk com permissão de SES, e o SDK a usaria automaticamente (mais seguro que chaves fixas).

5. Libere no Security Group do RDS a porta `5432` para o Security Group do Elastic Beanstalk
6. **Create environment** e aguarde

## Passo 5 — Validar

- Acesse a URL pública do ambiente
- Teste `/swagger-ui.html`, registre um usuário, cadastre um profissional/paciente e marque uma consulta — se o SES estiver configurado, o e-mail de confirmação deve chegar

## Passo 6 — Encerrar os recursos (evitar cobrança)

- Elastic Beanstalk → **Terminate environment**
- RDS → **Delete**
- (opcional) remova as identidades verificadas no SES e o usuário IAM criado
