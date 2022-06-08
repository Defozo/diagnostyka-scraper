# WebScraper for DIAGNOSTYKA's test results (wyniki.diag.pl)

[SEE IMPORTANT DISCLAIMER BEFORE USE.](#important-disclaimer)

This dockerized WebScraper written in Java allows you to download all PDF and CSV files from DIAGNOSTYKA's test results webpage (wyniki.diag.pl) to your Google Drive folder, and it allows you to convert downloaded CSV files into Google Spreadsheet.

![diagnostykascraper](https://user-images.githubusercontent.com/8215510/172599278-9fca0967-bd3c-4448-921f-aed18e1d7c7d.png)

## Table of contents

- [WebScraper for DIAGNOSTYKA's test results (wyniki.diag.pl)](#webscraper-for-diagnostykas-test-results-wynikidiagpl)
    - [Usage](#usage)
        - [Configure your Service Account to be able to access your Google Drive](#configure-your-service-account-to-be-able-to-access-your-google-drive)
        - [Single-container deployment](#single-container-deployment)
        - [Multi-container deployment using docker-compose](#multi-container-deployment-using-docker-compose)
        - [Add a cronjob or use other task scheduler to run the command periodically](#add-a-cronjob-or-use-other-task-scheduler-to-run-the-command-periodically)
        - [Automatically convert CSV files from Google Drive into Google Spreadsheet](#automatically-convert-csv-files-from-google-drive-into-google-spreadsheet)
    - [Contributors](#contributors)
    - [Donate](#donate)
    - [IMPORTANT DISCLAIMER](#important-disclaimer)

## Usage

#### Prerequisites

- Google account
- Docker

If you want to deploy Diagnostyka Scraper Java app and Chrome Driver separately, you'll also need:

- Docker-compose

### Configure your Service Account to be able to access your Google Drive

1. Create Google Project.
2. Create Service Account.
3. Download generated credentials with .p12 extension.
4. Create folder on Google Drive where you want to store test results and get its ID (last piece of URL).
5. Share access to the folder with your Service Account.
6. Choose either [single-container deployment](#use-single-container-deployment) or [multi-container deployment using docker-compose](#use-multi-container-deployment-using-docker-compose).

### Single-container deployment

#### To download test results into Google Drive folder replace strings with <> and run:

```bash
docker run -d --name diagnostyka-scraper \
-v "./<YOUR_CREDENTIALS_FILE_NAME>.p12:/credentials.p12" \
-e "DIAGNOSTYKA_USER_ID=<YOUR_LOGIN>" \
-e "DIAGNOSTYKA_USER_PASSWORD=<YOUR_PASSWORD>" \
-e "GOOGLE_DRIVE_FOLDER_ID=<YOUR_GOOGLE_DRIVE_FOLDER_ID>" \
-e "PRIVATE_KEY_FROM_P12_FILE_PATH=/credentials.p12" \
-e "SERVICE_ACCOUNT_ID=<YOUR_SERVICE_ACCOUNT_ID>@<YOUR_SERVICE_ACCOUNT_ID>.iam.gserviceaccount.com" \
-e "SHARED_DOWNLOADS_FOLDER=/diagnostyka_downloads" \
ghcr.io/defozo/diagnostyka-scraper:master
```

Go to [Add a cronjob or use other task scheduler to run the command periodically](#add-a-cronjob-or-use-other-task-scheduler-to-run-the-command-periodically).

### Multi-container deployment using docker-compose

This is how the Docker gods intended it. One container for the Java app, the other one for the Chrome Driver.

#### Start downloading test results into Google Drive folder

1. Download this repository (and unzip it if needed).
2. Change necessary environmental variables in docker-compose.yml.
3. Execute command `docker-compose up -d`.

### Add a cronjob or use other task scheduler to run the command periodically

Example for cronjob:

To edit:

`crontab -e`

Add `0 0 * * * YOUR_DOCKER_COMMAND/or/script/with/the/command.sh`

This will start docker container every day at 00:00.

You can use [crontab.guru](https://crontab.guru/#0_0_*_*_*) to simply edit cron schedule expressions.

### Automatically convert CSV files from Google Drive into Google Spreadsheet

1. Create a new Google Spreadsheet.
2. Click on Extensions -> Apps Script.
3. Copy content of [AppsScript-DiagnostykaImporter.gs](https://github.com/Defozo/diagnostyka-scraper/blob/master/AppsScript-DiagnostykaImporter.gs) from this repository to a new script in Apps Script.
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

Please note: This repository, source code, and built package is not associated with Diagnostyka Medical Laboratories. You may be in violation with their terms of service if you decide to use it.