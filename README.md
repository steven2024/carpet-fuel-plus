# CarpetFuelPlus

**CarpetFuelPlus** is a Minecraft plugin that enhances the functionality of carpets as fuel sources in furnaces. This plugin allows server admins to configure custom burn times for different carpet colors, providing greater control over fuel efficiency.

## Features

- **Configurable Burn Times:** Set different burn times for each carpet color.
- **Default Burn Time:** Apply a default burn time to all carpet colors.
- **Customizable Messages:** Modify plugin messages through the configuration file.
- **Autosmelter Support:** Addresses issues with hopper minecarts not delivering carpets fast enough in autosmelters, ensuring furnaces remain fueled long enough to finish smelting.

## Installation Instructions

1. **Download the Plugin:**
   - Download the latest version of the CarpetFuelPlus plugin JAR file from the [Releases](https://github.com/yourusername/carpet-fuel-plus/releases) page.

2. **Install the Plugin:**
   - Place the downloaded JAR file in your server's `plugins` directory.
   - Start your server to generate the default configuration file.
   - Edit the configuration file to set your desired burn times and messages.
   - Reload or restart your server to apply the changes.

## Commands

- **/setburntime <time_in_seconds> [color]**
  - **Description:** Sets the burn time for a specific carpet color or all carpets.
  - **Usage:** `/setburntime <time_in_seconds> [color]`
  - **Permission:** `carpetfuelplus.setburntime`

- **/checkburntime [color]**
  - **Description:** Checks the current burn time for a specific carpet color or all carpets.
  - **Usage:** `/checkburntime [color]`
  - **Permission:** `carpetfuelplus.checkburntime`

## Permissions

- `carpetfuelplus.setburntime`
  - **Description:** Allows setting the carpet burn time.
  - **Default:** OP

- `carpetfuelplus.checkburntime`
  - **Description:** Allows checking the current carpet burn time.
  - **Default:** True

## Configuration (`config.yml`)

```yaml
# Configuration file for CarpetFuelPlus plugin

# Default burn time for carpet in seconds
default-burn-time-seconds: 20

# Burn time for different carpet colors in seconds
carpet-burn-times:
  white: 20
  red: 20
  blue: 20
  # Add more colors as needed

# Custom messages
messages:
  setburntime-success: "Carpet burn time set to {time} seconds for {color} carpet."
  setburntime-error: "Invalid number format. Please enter a valid integer."
  checkburntime: "Current burn time for {color} carpet is {time} seconds."
  no-permission: "You do not have permission to use this command."
