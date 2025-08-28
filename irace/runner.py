import pathlib
import subprocess

IRACE_WORKING_DIR = pathlib.Path("../")

def compile_code(source_folder) -> None:
    subprocess.run(
        ["mvn", "clean", "compile", "package"],
        capture_output=True,
        text=True,
        cwd=source_folder,
        check=True,
    )


def main():
    compile_code(IRACE_WORKING_DIR)
    print("calling Irace parameter optimizer")
    subprocess.run(
        [
            "irace",
            "--scenario=irace/scenario.txt",
            "--instanceDir datasets"
        ],
        cwd=IRACE_WORKING_DIR,
        check=True
    )


main()