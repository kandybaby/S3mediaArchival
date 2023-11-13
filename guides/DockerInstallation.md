
# Installing Docker

Docker can be installed either using Docker Desktop (a graphical user interface application) or via the command line. Windows and macOS users will use Docker Desktop, while Linux users typically install Docker through the command line.

## For Windows Users: Using Docker Desktop

1. **Download Docker Desktop for Windows**
    - Go to the [Docker website](https://www.docker.com/products/docker-desktop) and download the Docker Desktop installer.
    - Ensure your system meets the necessary requirements, particularly for Windows 10 and 11 versions.

2. **Install Docker Desktop**
    - Run the installer and follow the on-screen instructions.
    - Docker Desktop requires certain permissions during installation.

3. **Configure and Run Docker Desktop**
    - After installation, launch Docker Desktop. It may take a few minutes to initialize.
    - A Docker icon in the system tray indicates that Docker Desktop is running.

4. **Verify Installation**
    - Open a command prompt or PowerShell and type `docker --version`.
    - Run `docker run hello-world` to ensure Docker can pull and run images.

## For macOS Users: Using Docker Desktop

1. **Download Docker Desktop for macOS**
    - Visit the [Docker website](https://www.docker.com/products/docker-desktop) and download Docker Desktop for macOS.
    - Check for any specific macOS version requirements.

2. **Install and Run Docker Desktop**
    - Open the downloaded `.dmg` file and drag Docker to the Applications folder.
    - Run Docker from the Applications folder; it will initialize on the first launch.

3. **Verify Installation**
    - Open Terminal and type `docker --version`.
    - Test with `docker run hello-world`.


_Note: While it's possible to run Docker on Windows or macOS without Docker Desktop, it's a bit more complicated and this guide will not cover that setup._


## For Linux Users: Command-Line Installation

1. **Update Software Repositories**
    - Open Terminal and update package lists with
      ```
      sudo apt-get update
      ```

2. **Install Docker**
    - Install Docker using
      ```
      sudo apt-get install docker-ce docker-ce-cli containerd.io
      ```

3. **Manage Docker Service**
    - Start Docker using
      ```
      sudo systemctl start docker
      ```
    - Enable Docker on boot with
      ```
      sudo systemctl enable docker
      ```

4. **Add User to Docker Group (Optional)**
    - Add your user to the Docker group with
      ```
      sudo usermod -aG docker $USER
      ```
    - Log out and back in to apply changes.

5. **Verify Installation**
    - Run `docker --version`.
    - Test Docker with `docker run hello-world`.
