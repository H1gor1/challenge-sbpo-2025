import pathlib
import subprocess
from textwrap import dedent

PROJECT_DIR = pathlib.Path("../")
SCENARIO_FILE = pathlib.Path("scenario.txt")
OLD_ENV_FILE = pathlib.Path(".env")


def compile_code(source_folder) -> None:
    subprocess.run(
        ["mvn", "clean", "compile", "package"],
        capture_output=True,
        text=True,
        cwd=source_folder,
        check=True,
    )


def main():
    if OLD_ENV_FILE.exists():
        OLD_ENV_FILE.unlink()  # remove the old env file to avoid interfering with the tests

    compile_code(PROJECT_DIR)
    print("calling Irace parameter optimizer")
    r_command = dedent(
        f"""
        library(irace)
        scenario <- irace::readScenario("{SCENARIO_FILE.resolve()}")
        irace_result <- irace::irace(scenario)

        best <- irace::getFinalElites(irace_result, n = 1)[[1]]
        env_lines <- paste(names(best), best, sep="=")
        writeLines(env_lines, con=".env")
        """
    )
    subprocess.run(["Rscript", "-e", r_command], check=True)


main()
