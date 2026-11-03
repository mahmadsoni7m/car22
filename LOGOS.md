# Logo folders (add your own logos here)

Folder: `app/src/main/assets/logos/`

Drop PNG/WEBP/JPG files with **exactly** these names. No code changes are needed: the app picks them up on the next build.

| Folder | File name (without extension) | Where it appears |
|---|---|---|
| `app/` | `app_logo` | Main logo: splash, PIN screen, welcome, side menu, About (PNG, transparent background) |
| `transfers/` | `transfer_phone` | Transfer by phone number |
| `transfers/` | `transfer_card` | Transfer by card number |
| `transfers/` | `transfer_interbank` | Transfer to other banks |
| `services/` | `mobile` | Mobile |
| `services/` | `internet` | Internet |
| `services/` | `electricity` | Electricity |
| `services/` | `water` | Water |
| `services/` | `gas` | Gas |
| `services/` | `tv` | Television |
| `services/` | `education` | Education |
| `services/` | `transport` | Transport |
| `tiles/` | `payments` | Payments tile |
| `tiles/` | `cards` | Cards tile |
| `tiles/` | `topup` | Top up tile |
| `tiles/` | `withdraw` | Withdraw tile |
| `tiles/` | `statistics` | Statistics tile |
| `tiles/` | `history` | History tile |
| `tiles/` | `notifications` | Notifications tile |
| `tiles/` | `security` | Security tile |
| `partners/` | `qr_partner_1` | QR screen, bottom strip, logo 1 |
| `partners/` | `qr_partner_2` | QR screen, bottom strip, logo 2 |
| `partners/` | `qr_partner_3` | QR screen, bottom strip, logo 3 |

Recommended: square PNG 256x256 with transparent background (partners: wide PNG, height 64 px).

## App icon on the phone home screen
Replace `app/src/main/res/drawable/ic_launcher_foreground.xml` with a PNG named `ic_launcher_foreground.png` (delete the .xml), or use Android Studio: New -> Image Asset.

## Rights
Use only logos you own or have written permission to use (your own brand, or partners that agreed). Do not use another bank's or company's logo or name for your app or inside it unless you have a license: it is a trademark issue and can look like impersonation of a real bank.
