<p align="center">
	<img src="data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KICAgIDxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB2aWV3Qm94PSIwIDAgODAwIDIwMCI+CiAgICAgICAgPGRlZnM+CiAgICAgICAgICAgIDxsaW5lYXJHcmFkaWVudCBpZD0iYmctZ3JhZGllbnQiIHgxPSIwJSIgeTE9IjAlIiB4Mj0iMTAwJSIgeTI9IjEwMCUiPgogICAgICAgICAgICAgICAgPHN0b3Agb2Zmc2V0PSIwJSIgc3R5bGU9InN0b3AtY29sb3I6IzQxNThEMDtzdG9wLW9wYWNpdHk6MSIgLz4KICAgICAgICAgICAgICAgIDxzdG9wIG9mZnNldD0iNTAlIiBzdHlsZT0ic3RvcC1jb2xvcjojQzg1MEMwO3N0b3Atb3BhY2l0eToxIiAvPgogICAgICAgICAgICAgICAgPHN0b3Agb2Zmc2V0PSIxMDAlIiBzdHlsZT0ic3RvcC1jb2xvcjojRkZDQzcwO3N0b3Atb3BhY2l0eToxIiAvPgogICAgICAgICAgICA8L2xpbmVhckdyYWRpZW50PgogICAgICAgICAgICA8ZmlsdGVyIGlkPSJzaGFkb3ciPgogICAgICAgICAgICAgICAgPGZlRHJvcFNoYWRvdyBkeD0iMCIgZHk9IjQiIHN0ZERldmlhdGlvbj0iNCIgZmxvb2Qtb3BhY2l0eT0iMC4yNSIgLz4KICAgICAgICAgICAgPC9maWx0ZXI+CiAgICAgICAgPC9kZWZzPgogICAgICAgIDxyZWN0IHdpZHRoPSI4MDAiIGhlaWdodD0iMjAwIiBmaWxsPSJ1cmwoI2JnLWdyYWRpZW50KSIgcng9IjE1IiByeT0iMTUiLz4KICAgICAgICA8dGV4dCB4PSI0MDAiIHk9IjEwMCIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjQ4IgogICAgICAgIGZvbnQtd2VpZ2h0PSJib2xkIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIgogICAgICAgIGZpbGw9IiNGRkZGRkYiIGZpbHRlcj0idXJsKCNzaGFkb3cpIj5LUjJGQTwvdGV4dD4KICAgIDwvc3ZnPg==" alt="kr2fa-banner" width="800"></p>
	
<p align="left">
	<em><code>A lightweight staff-only 2FA plugin for Velocity 3 (MC 1.21.x). Premium-only (online-mode) friendly.</code></em>
</p>
<p align="left">
	<!-- Shields.io badges disabled, using skill icons. --></p>
<p align="left">Built with the tools and technologies:</p>
<p align="left">
	<a href="https://skillicons.dev">
		<img src="https://skillicons.dev/icons?i=java,md">
	</a></p>
<br>

##  Quick Links

- [ Overview](#-overview)
- [ Features](#-features)
- [ Project Structure](#-project-structure)
  - [ Project Index](#-project-index)
- [ Getting Started](#-getting-started)
  - [ Prerequisites](#-prerequisites)
  - [ Installation](#-installation)
  - [ Usage](#-usage)
  - [ Testing](#-testing)
- [ Project Roadmap](#-project-roadmap)
- [ Contributing](#-contributing)
- [ License](#-license)
- [ Acknowledgments](#-acknowledgments)

---

##  Overview

Kr2FA is a Two-Factor Authentication plugin explicitly designed for Minecraft Velocity proxy servers. It provides an additional layer of security for staff members by requiring TOTP-based authentication (compatible with Google Authenticator/Authy) before granting access to backend servers. The plugin blocks staff from connecting to game servers or executing commands until they complete 2FA verification, preventing unauthorized access even if credentials are compromised.

---

##  Features

- **Staff-Only 2FA**: Only prompts users with configurable staff permissions (`staff`, `moderator`, `admin`, `helper`, or `velocity2fa.staff`)
- **TOTP Authentication**: Uses industry-standard Time-based One-Time Passwords compatible with Google Authenticator, Authy, and similar apps
- **Server Connection Blocking**: Blocks staff from joining backend servers until 2FA verification is complete
- **Command Restriction**: Blocks all commands (except `/2fa`) until authentication is successful
- **Persistent Secrets**: Securely stores TOTP secrets in `plugins/Kr2FA/secrets.json`
- **Session Cache**: Configurable session duration to avoid re-verifying too often (default 12h)
- **Admin Management**: Dedicated admin command (`/2fa-admin`) for managing staff 2FA settings
- **Limbo Server Support**: Allows players to connect to a designated limbo/lobby server while pending authentication

- **Configurable Message Prefix**: Prepend a custom, editable prefix to all plugin messages for clear branding

---

##  Project Structure

```sh
└── Kr2FA/
    ├── .github
    │   └── workflows
    ├── LICENSE
    ├── README.md
    ├── pom.xml
    ├── src
    │   └── main
    └── target
        ├── classes
        └── maven-status
```


###  Project Index
<details open>
	<summary><b><code>KR2FA/</code></b></summary>
	<details> <!-- __root__ Submodule -->
		<summary><b>__root__</b></summary>
		<blockquote>
			<table>
			</table>
		</blockquote>
	</details>
	<details> <!-- .github Submodule -->
		<summary><b>.github</b></summary>
		<blockquote>
			<details>
				<summary><b>workflows</b></summary>
				<blockquote>
					<table>
					<tr>
						<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/.github/workflows/build.yml'>build.yml</a></b></td>
						<td>GitHub Actions workflow for building, testing, and packaging the Velocity plugin with automatic version bumping</td>
					</tr>
					</table>
				</blockquote>
			</details>
		</blockquote>
	</details>
	<details> <!-- target Submodule -->
		<summary><b>target</b></summary>
		<blockquote>
			<details>
				<summary><b>maven-status</b></summary>
				<blockquote>
					<details>
						<summary><b>maven-compiler-plugin</b></summary>
						<blockquote>
							<details>
								<summary><b>compile</b></summary>
								<blockquote>
									<details>
										<summary><b>default-compile</b></summary>
										<blockquote>
											<table>
											<tr>
												<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/target/maven-status/maven-compiler-plugin/compile/default-compile/inputFiles.lst'>inputFiles.lst</a></b></td>
												<td>Maven compiler plugin input file list for tracking source files</td>
											</tr>
											</table>
										</blockquote>
									</details>
								</blockquote>
							</details>
						</blockquote>
					</details>
				</blockquote>
			</details>
			<details>
				<summary><b>classes</b></summary>
				<blockquote>
					<table>
					<tr>
						<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/target/classes/config.json'>config.json</a></b></td>
						<td>Plugin configuration file defining session TTL and limbo server settings</td>
					</tr>
					<tr>
						<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/target/classes/velocity-plugin.json'>velocity-plugin.json</a></b></td>
						<td>Velocity plugin metadata descriptor</td>
					</tr>
					</table>
				</blockquote>
			</details>
		</blockquote>
	</details>
	<details> <!-- src Submodule -->
		<summary><b>src</b></summary>
		<blockquote>
			<details>
				<summary><b>main</b></summary>
				<blockquote>
					<details>
						<summary><b>resources</b></summary>
						<blockquote>
							<table>
							<tr>
								<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/src/main/resources/config.json'>config.json</a></b></td>
								<td>Default plugin configuration with session TTL and limbo server settings</td>
							</tr>
							<tr>
								<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/src/main/resources/velocity-plugin.json'>velocity-plugin.json</a></b></td>
								<td>Velocity plugin descriptor defining plugin metadata and dependencies</td>
							</tr>
							</table>
						</blockquote>
					</details>
					<details>
						<summary><b>java</b></summary>
						<blockquote>
							<details>
								<summary><b>com</b></summary>
								<blockquote>
									<details>
										<summary><b>queazified</b></summary>
										<blockquote>
											<details>
												<summary><b>velocity2fa</b></summary>
												<blockquote>
													<table>
													<tr>
														<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/src/main/java/com/queazified/velocity2fa/TwoFactorManager.java'>TwoFactorManager.java</a></b></td>
														<td>Manages TOTP secret generation, storage, and verification using Google Authenticator library</td>
													</tr>
													<tr>
														<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/src/main/java/com/queazified/velocity2fa/TwoFactorCommand.java'>TwoFactorCommand.java</a></b></td>
														<td>Handles player 2FA commands for setup and code verification</td>
													</tr>
													<tr>
														<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/src/main/java/com/queazified/velocity2fa/Velocity2FA.java'>Velocity2FA.java</a></b></td>
														<td>Main plugin class handling initialization, event listeners, and authentication state management</td>
													</tr>
													<tr>
														<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/src/main/java/com/queazified/velocity2fa/AdminCommand.java'>AdminCommand.java</a></b></td>
														<td>Administrative commands for managing player 2FA settings (reset, status, etc.)</td>
													</tr>
													<tr>
														<td><b><a href='https://github.com/KryptonSystems/Kr2FA/blob/master/src/main/java/com/queazified/velocity2fa/ConfigManager.java'>ConfigManager.java</a></b></td>
														<td>Handles loading and parsing of plugin configuration from JSON files</td>
													</tr>
													</table>
												</blockquote>
											</details>
										</blockquote>
									</details>
								</blockquote>
							</details>
						</blockquote>
					</details>
				</blockquote>
			</details>
		</blockquote>
	</details>
</details>

---
##  Getting Started

###  Prerequisites

Before getting started with Kr2FA, ensure your runtime environment meets the following requirements:

- **Programming Language:** Java 21 or higher (build may require matching JDK)
- **Build Tool:** Maven 3.6+
- **Server:** Velocity 3.1.1+ proxy server


###  Installation

Install Kr2FA using one of the following methods:

**Build from source:**

1. Clone the Kr2FA repository:
```sh
❯ git clone https://github.com/KryptonSystems/Kr2FA
```

2. Navigate to the project directory:
```sh
❯ cd Kr2FA
```

3. Build the project using Maven:
```sh
❯ mvn clean package
```

4. Copy the generated JAR to your Velocity plugins folder:
```sh
❯ cp target/Velocity2FA-*.jar /path/to/velocity/plugins/
```


###  Usage
1. Start or restart your Velocity proxy server
2. Grant staff members the appropriate permission (any of the following will work):
   ```sh
   /lp group staff permission set staff true
   # OR use any of these alternatives:
   # /lp group staff permission set moderator true
   # /lp group staff permission set admin true
   # /lp group staff permission set helper true
   # /lp group staff permission set velocity2fa.staff true
   ```
3. When staff members first join, they'll be prompted to set up 2FA:
   ```sh
   /2fa setup
   ```
4. Staff members verify their TOTP code on each session:
   ```sh
   /2fa <6-digit-code>
   ```

###  Configuration

Edit `plugins/Velocity2FA/config.json` after first run. New option:

- `messagePrefix`: A short tag prepended to all player/console messages sent by the plugin. Default: `[Kr2FA]`.

Example snippet:
```json
{
	"serverName": "Proxy-01",
	"limboServer": "Hub",
	"messagePrefix": "[Kr2FA]",
	"issuerName": "Kr2FA"
}
```

###  Testing
Run the test suite using the following command:
```sh
❯ mvn test
```

---
##  Project Roadmap

- [X] **`Task 1`**: <strike>Implement core TOTP authentication.</strike>
- [X] **`Task 2`**: <strike>Add session caching for improved user experience.</strike>
- [X] **`Task 3`**: <strike>Implement command blocking until authentication.</strike>
- [ ] **`Task 4`**: Add backup codes for account recovery.
- [ ] **`Task 5`**: Implement IP-based session trust.
- [ ] **`Task 6`**: Add Discord/webhook notifications for 2FA events.

---

##  Contributing

- **💬 [Join the Discussions](https://github.com/KryptonSystems/Kr2FA/discussions)**: Share your insights, provide feedback, or ask questions.
- **🐛 [Report Issues](https://github.com/KryptonSystems/Kr2FA/issues)**: Submit bugs found or log feature requests for the `Kr2FA` project.
- **💡 [Submit Pull Requests](https://github.com/KryptonSystems/Kr2FA/blob/main/CONTRIBUTING.md)**: Review open PRs, and submit your own PRs.

<details closed>
<summary>Contributing Guidelines</summary>

1. **Fork the Repository**: Start by forking the project repository to your github account.
2. **Clone Locally**: Clone the forked repository to your local machine using a git client.
   ```sh
   git clone https://github.com/KryptonSystems/Kr2FA
   ```
3. **Create a New Branch**: Always work on a new branch, giving it a descriptive name.
   ```sh
   git checkout -b new-feature-x
   ```
4. **Make Your Changes**: Develop and test your changes locally.
5. **Commit Your Changes**: Commit with a clear message describing your updates.
   ```sh
   git commit -m 'Implemented new feature x.'
   ```
6. **Push to github**: Push the changes to your forked repository.
   ```sh
   git push origin new-feature-x
   ```
7. **Submit a Pull Request**: Create a PR against the original project repository. Clearly describe the changes and their motivations.
8. **Review**: Once your PR is reviewed and approved, it will be merged into the main branch. Congratulations on your contribution!
</details>

<details closed>
<summary>Contributor Graph</summary>
<br>
<p align="left">
   <a href="https://github.com/KryptonSystems/Kr2FA/graphs/contributors">
      <img src="https://contrib.rocks/image?repo=KryptonSystems/Kr2FA">
   </a>
</p>
</details>

---

##  License

This project is protected under the [MIT](https://choosealicense.com/licenses/mit/) License. For more details, refer to the [LICENSE](LICENSE) file.

---

##  Acknowledgments

- [Google Authenticator Library](https://github.com/wstrange/GoogleAuth) for TOTP implementation
- [ZXing](https://github.com/zxing/zxing) for QR code generation
- [Velocity](https://velocitypowered.com/) for the proxy platform
- [Gson](https://github.com/google/gson) for JSON handling
- All contributors and the Minecraft server community

---
