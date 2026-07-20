# Home

## 🏠 What is SimplyMines?

**SimplyMines** is a modern, lightweight, and highly configurable skymines/prison plugin for Paper 1.21+.\
It lets you build classic prison mines or simple regenerating resource pits, without the lag that usually comes with mass block regeneration.

<figure><img src="https://cdn.modrinth.com/data/cached_images/c18b4531e84490a19a5ef6140af4551371178440.gif" alt=""><figcaption><p>Lag-free mine resets in action</p></figcaption></figure>

This documentation contains a complete guide to installing, configuring, and building on top of SimplyMines. If you need further assistance, feel free to reach out on the plugin's support page.

{% hint style="success" %}
**New in 1.1.0:** a full [Developer API](api/README.md), CraftEngine support, mine/reset requirements, blocks-mined tracking, and mine renaming. See the changelog at the bottom of this page.
{% endhint %}

***

## ✨ Why SimplyMines?

* ⚡ **Lag-free resets** — a Workload System spreads block placement across ticks with a hard 2.5ms/tick budget, instead of freezing the server on a big mine reset.
* 🖱️ **Fully GUI-driven** — every setting (reset timer, block composition, warnings, physics, requirements, teleport point) can be edited in-game, no config file editing required.
* 🧱 **Custom block support** — works with vanilla blocks as well as ItemsAdder, Nexo, and CraftEngine custom blocks.
* 🧩 **Requirements system** — gate who can mine where (permission / tool Efficiency), and decide what triggers a reset (time / percentage mined).
* 👨‍💻 **Developer API** — other plugins can read mine state, hook into resets, and even register their own custom requirement types.
* 🔤 **PlaceholderAPI support** — display mine timers, status, and stats anywhere placeholders are supported.

***

## 🧭 Where to go next

* 🚀 New to the plugin? Start with [Getting Started](general-usage/getting-started.md).
* ⛏️ Want to create your first mine? See [Creating & Managing Mines](general-usage/creating-mines.md).
* 🔐 Setting up permissions for staff/players? See [Permissions](general-usage/permissions.md).
* 👨‍💻 Building an addon or integration? See the [Developer API](api/README.md).

Every mine is stored as its own JSON file under `plugins/SimplyMines/mines/<name>.json`. You generally never need to touch these by hand — use the in-game GUI or the [API](api/README.md).

***
