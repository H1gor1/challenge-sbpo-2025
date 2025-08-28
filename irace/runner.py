import pathlib
import subprocess
from textwrap import dedent

PROJECT_DIR = pathlib.Path("../")
SCENARIO_FILE = pathlib.Path("scenario.txt")

def compile_code(source_folder) -> None:
    subprocess.run(
        ["mvn", "clean", "compile", "package"],
        capture_output=True,
        text=True,
        cwd=source_folder,
        check=True,
    )


def main():
    compile_code(PROJECT_DIR)
    print("calling Irace parameter optimizer")
    r_command = dedent(
        f"""
        library(irace)
        scenario <- irace::readScenario("{SCENARIO_FILE.resolve()}")
        irace::irace(scenario)
        """
    )
    subprocess.run(
        ["Rscript", "-e", r_command],
        check=True
    )


main()