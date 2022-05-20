# WebScraper for DIAGNOSTYKA's test results (wyniki.diag.pl)

[SEE IMPORTANT DISCLAIMER BEFORE USE.](#important-disclaimer)

This dockerized WebScraper written in Java allows you to download all PDF and CSV files from DIAGNOSTYKA's test results webpage (wyniki.diag.pl) to your Google Drive folder, and it allows you to convert downloaded CSV files into Google Spreadsheet.

## Usage

### Prerequisites

- Google account
- Docker
- Docker-compose

### Start downloading test results into Google Drive folder

1. Create Google Project.
2. Create Service Account.
3. Create folder on Google Drive where you want to store test results and get its ID (last piece of URL).
4. Share access to the folder with your Service Account.
5. Download this repository (and unzip it if needed).
6. Change necessary environmental variables in docker-compose.yml.
7. Execute command `docker-compose up -d`.

Add cronjob or use other task scheduler to run the command periodically.

### Automatically convert CSV files into Google Spreadsheet

1. Create new Google Spreadsheet.
2. Click Extensions -> Apps Script.
3. Copy content of AppsScript-DiagnostykaImporter.gs from this repository to a new script in Apps Script.
4. Click on Rules to add a rule to run the script periodically.

## Contributors

Contributions are welcome.

## Donate

You can support my work by buying me coffee.

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/donate/?business=6UJGLQTZ4U43U&no_recurring=0&currency_code=PLN)

## IMPORTANT DISCLAIMER

THIS SOURCE CODE AND ASSOCIATED SOFTWARE IS PROVIDED "AS IS". YOU USE IT AT YOUR OWN RISK. YOU SHOULD NOT RELY ON THE RESULTS DOWNLOADED BY THIS SOFTWARE. ALWAYS USE ORIGINAL DOCUMENTS PROVIDED BY MEDICAL LABORATORIES WHEN CONSULTING WITH HEALTHCARE PROVIDERS.

No warranties of any kind whatsoever are made as to the results that you will obtain from relying upon the covered code (or any information or content obtained by way of the covered code), including but not limited to compliance with privacy laws or regulations or laboratory and clinical care industry standards and protocols. Use of the covered code is not a substitute for appropriately-trained and registered professional medical laboratory service and healthcare providers, standard practice, quality assurance guidelines or professional judgment. Any decision with regard to the appropriateness of treatment, or the validity or reliability of information or content made available by the covered code, is the sole responsibility of the appropriately-trained and registered professional medical laboratory personnel and health care providers.

Under no circumstances and under no legal theory, whether tort (including negligence), contract, or otherwise, shall any Contributor, or anyone who distributes Covered Software as permitted by the license, be liable to you for any indirect, special, incidental, consequential damages of any character including, without limitation, damages for loss of goodwill, work stoppage, computer failure or malfunction, or any and all other damages or losses of any nature whatsoever (direct or otherwise) on account of or associated with the use or inability to use the covered content (including, without limitation, the use of information or content made available by the covered code, all documentation associated therewith, and the failure of the covered code to comply with privacy laws and regulations or clinical care industry standards and protocols), even if such party shall have been informed of the possibility of such damages.