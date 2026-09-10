"""Create disposable build inputs for untrusted PR validation, never for distribution."""
import json
import copy
import os
from pathlib import Path
import subprocess


def main():
    if os.environ.get("CI") != "true":
        raise SystemExit("This command is restricted to CI runners.")
    root = Path.cwd()
    targets = [root / "app/google-services.json", root / "app/release.properties", root / "app/keys/ci.jks", root / "app/src/dev/google-services.json"]
    if any(path.exists() for path in targets):
        raise SystemExit("Refusing to replace existing Firebase or signing files.")
    client = {
        "client_info": {
            "mobilesdk_app_id": "1:123456789:android:0123456789abcdef",
            "android_client_info": {"package_name": "com.luminor.tavernquest"},
        },
        "oauth_client": [{"client_id": "123456789-ci-only.apps.googleusercontent.com", "client_type": 3}],
        "api_key": [{"current_key": "AIza" + "0" * 35}],
    }
    config = {
        "project_info": {"project_number": "123456789", "project_id": "demo-tavernquest", "storage_bucket": "demo-tavernquest.appspot.com"},
        "client": [client], "configuration_version": "1",
    }
    targets[2].parent.mkdir(parents=True, exist_ok=True)
    subprocess.run([
        "keytool", "-genkeypair", "-noprompt", "-keystore", str(targets[2]),
        "-storepass", "ci-only-password", "-keypass", "ci-only-password",
        "-alias", "ci", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2",
        "-dname", "CN=CI ONLY DO NOT DISTRIBUTE",
    ], check=True)
    targets[0].write_text(json.dumps(config, indent=2), encoding="utf-8")
    dev = copy.deepcopy(config)
    dev["client"][0]["client_info"]["android_client_info"]["package_name"] += ".dev"
    targets[3].parent.mkdir(parents=True, exist_ok=True)
    targets[3].write_text(json.dumps(dev, indent=2), encoding="utf-8")
    targets[1].write_text("storeFile=keys/ci.jks\nstorePassword=ci-only-password\nkeyAlias=ci\nkeyPassword=ci-only-password\n", encoding="utf-8")


if __name__ == "__main__":
    main()
