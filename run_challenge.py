import os
import subprocess
import sys
import platform

# Paths to the libraries
CPLEX_PATH = "$HOME/CPLEX_Studio2211/opl/bin/arm64_osx/"
OR_TOOLS_PATH = "$HOME/Documents/or-tools/build/lib/"

USE_CPLEX = True
USE_OR_TOOLS = True

MAX_RUNNING_TIME = "605s"

class TimeoutError(Exception):
    """Custom exception for timeout errors."""
    pass

def compile_code(source_folder):
    print(f"Compiling code in {source_folder}...")

    # Run Maven compile without changing directory
    result = subprocess.run(
        ["mvn", "clean", "package"],
        capture_output=True,
        text=True,
        cwd=source_folder
    )

    if result.returncode != 0:
        print("Maven compilation failed:")
        print(result.stderr)
        return False

    print("Maven compilation successful.")
    return True


def run_benchmark(source_folder, input_folder, output_folder, population_size, generations, individuals, crossover_rate, mutation_rate):
    # Criar pasta de saída, se necessário
    if not os.path.exists(output_folder):
        os.makedirs(output_folder)

    # Configurar o caminho das bibliotecas (se necessário)
    if USE_CPLEX and USE_OR_TOOLS:
        libraries = f"{OR_TOOLS_PATH}:{CPLEX_PATH}"
    elif USE_CPLEX:
        libraries = CPLEX_PATH
    elif USE_OR_TOOLS:
        libraries = OR_TOOLS_PATH

    if platform.system() == "Darwin":
        timeout_command = "gtimeout"
    else:
        timeout_command = "timeout"

    # Caminho do JAR
    jar_path = os.path.join(source_folder, "target", "ChallengeSBPO2025-1.0-shaded.jar")

    for filename in os.listdir(input_folder):
        if filename.endswith(".txt"):
            print(f"Running {filename}")
            input_file = os.path.join(input_folder, filename)
            output_file = os.path.join(output_folder, f"{os.path.splitext(filename)[0]}.txt")

            # Adicionando os parâmetros do iRace
            cmd = [
                timeout_command,
                MAX_RUNNING_TIME,
                "java",
                "-Xmx16g",
                "-jar",
                jar_path,
                input_file,
                output_file,
                str(population_size),
                str(generations),
                str(individuals),
                str(crossover_rate),
                str(mutation_rate)
            ]

            # Se CPLEX ou OR-Tools estiverem ativados, configurar `-Djava.library.path`
            if USE_CPLEX or USE_OR_TOOLS:
                cmd.insert(3, f"-Djava.library.path={libraries}")

            result = subprocess.run(
                cmd,
                stderr=subprocess.PIPE,
                text=True,
                cwd=source_folder  # Define o diretório de execução
            )

            # Verifica erro de timeout (código de saída 124 indica timeout)
            if result.returncode == 124:
                error_msg = f"Execution timed out after {MAX_RUNNING_TIME} for {input_file}"
                print(error_msg)
                raise TimeoutError(error_msg)
            elif result.returncode != 0:
                print(f"Execution failed for {input_file}:")
                print(result.stderr)
                raise RuntimeError(f"Execution failed for {input_file}: {result.stderr}")

if __name__ == "__main__":
    if len(sys.argv) != 9:
        print(f"Erro: esperado 9 argumentos, mas recebeu {len(sys.argv)}.")
        print("Uso correto: python run_challenge.py <source_folder> <input_folder> <output_folder> <population_size> <generations> <individuals> <crossover_rate> <mutation_rate>")
        sys.exit(1)

    source_folder = os.path.abspath(sys.argv[1])
    input_folder = os.path.abspath(sys.argv[2])
    output_folder = os.path.abspath(sys.argv[3])
    population_size = int(sys.argv[4])
    generations = int(sys.argv[5])
    individuals = int(sys.argv[6])
    crossover_rate = float(sys.argv[7])
    mutation_rate = float(sys.argv[8])

    if compile_code(source_folder):
        # Caminho do JAR gerado pelo Maven
        jar_path = os.path.join(source_folder, "target", "ChallengeSBPO2025-1.0.jar")

        # Verificação da existência do JAR antes de tentar executá-lo
        if not os.path.exists(jar_path):
            print(f"Erro: o JAR {jar_path} não foi encontrado. Certifique-se de que a compilação ocorreu corretamente.")
            sys.exit(1)

        # Se o JAR existir, prosseguir para rodar os benchmarks
        run_benchmark(source_folder, input_folder, output_folder, population_size, generations, individuals, crossover_rate, mutation_rate)