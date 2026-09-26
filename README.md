# Kayji-SupportPlugin-v2

Plugin Minecraft (Spigot) hệ thống **yêu cầu hỗ trợ (support ticket)**: người chơi gửi yêu cầu trong game, staff xử lý qua **GUI quản lý**, lưu vào **SQLite/MySQL** và thông báo qua **Discord webhook**.

> **Tác giả:** Kayji_Tizi · **Phiên bản:** 1.1 · **API:** 1.16+ · **Java:** 8

## Tính năng

- **Gửi yêu cầu** bằng `/support <nội dung>` — mỗi yêu cầu có ID riêng, lưu vĩnh viễn vào database.
- **4 trạng thái**: `OPEN` (Mở) → `IN_PROGRESS` (Đang xử lý) → `RESOLVED` (Đã giải quyết) → `CLOSED` (Đã đóng).
- **Quét trạng thái bằng tiếng Việt không dấu hoặc tiếng Anh**: `dangcho`/`open`, `dangxuly`/`inprogress`, `dagiaiquyet`/`done`, `dadong`/`closed`.
- **GUI quản lý** (`/supportgui`) cho staff: xem danh sách ticket, bấm để cập nhật trạng thái.
- **Thông báo Discord webhook** khi có yêu cầu mới + log ra console (tắt bằng `notify-console`).
- **Database linh hoạt**: SQLite (mặc định, không cần cài gì) hoặc MySQL qua **HikariCP** — chỉ đổi `database.type` trong config.
- **Toàn bộ tin nhắn tùy chỉnh** trong `messages` của `config.yml`.

## Bảng lệnh

Gõ trong game với dấu `/`:

| Lệnh | Quyền | Mô tả |
| --- | --- | --- |
| `/support <nội dung>` | `supportplugin.use` | Gửi yêu cầu hỗ trợ, trả về ID |
| `/supportstatus <id>` | `supportplugin.use` | Xem trạng thái yêu cầu theo ID |
| `/supportupdate <id> <status>` | `supportplugin.admin` | Cập nhật trạng thái (vd. `open`, `dangxuly`, `done`, `closed`) |
| `/supportgui` | `supportplugin.manage` | Mở GUI quản lý ticket (staff) |
| `/supportreload` | `supportplugin.reload` | Tải lại `config.yml` |

> Quyền mặc định: `supportplugin.use` = **mọi người**; `supportplugin.manage`, `supportplugin.admin`, `supportplugin.reload` = **op**.

## Cấu hình

```yaml
support:
  webhook-url: "https://discord.com/api/webhooks/your_webhook_id/your_webhook_token"
  notify-console: true
  date-format: "dd/MM/yyyy HH:mm:ss"

messages:
  support-received: "&aYêu cầu của bạn đã được gửi. ID: &e{id}&a"
  # ... xem file đầy đủ

database:
  type: sqlite                     # sqlite | mysql
  sqlite:
    file: "data/support.db"
  mysql:
    host: "localhost"
    port: 3306
    database: "supportdb"
    user: "root"
    password: "password"           # ⚠️ đổi khi dùng MySQL, không commit mật khẩu thật
```

## Cài đặt

```bash
mvn clean package
```

1. Copy `target/Kayji-Support-v2-v2.1.jar` vào thư mục `plugins/`.
2. Chạy server một lần để tạo `config.yml`.
3. (Tùy chọn) Dán Discord webhook URL vào `support.webhook-url`.
4. (Tùy chọn) Đổi `database.type: mysql` và điền thông tin kết nối.
5. Restart server.

> Maven Shade Plugin đóng gói HikariCP, SQLite JDBC và MySQL Connector vào jar (loại trừ `spigot-api`).

## Cấu trúc dự án

```
├── pom.xml                                  Maven + Shade (release 8)
└── src/main
    ├── java/com/aefamily/support
    │   ├── SupportPlugin.java               Lớp chính, đăng ký lệnh
    │   ├── SupportCommand.java              /support — tạo yêu cầu
    │   ├── StatusCommand.java               /supportstatus
    │   ├── UpdateCommand.java               /supportupdate
    │   ├── SupportGUICommand.java           /supportgui
    │   ├── ReloadCommand.java               /supportreload
    │   ├── GUIManager.java                  GUI quản lý cho staff
    │   ├── SupportManager.java              Điều phối ticket
    │   ├── SupportRequest.java / Status.java / Priority.java   Model
    │   ├── DiscordWebhook.java              Gửi embed webhook
    │   └── db/                              DAO (SQLite/MySQL) + HikariCP
    └── resources
        ├── plugin.yml                       Lệnh + quyền
        └── config.yml                       Webhook, tin nhắn, database
```

## Giấy phép

[GNU General Public License v3.0](LICENSE)
