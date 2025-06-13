library(irace)

# Definir o objeto scenario
scenario <- list(
  targetRunner = "./run_challenge.py",  # Seu script Python
  parameters = "parameters.txt",        # Arquivo de parâmetros
  maxExperiments = 500,                 # Número de testes
  logFile = "irace.log",                 # Log de execução
  outputFolder = "irace_output"          # Pasta onde os resultados serão salvos
)

# Agora chamamos o iRace corretamente
irace(scenario = scenario)