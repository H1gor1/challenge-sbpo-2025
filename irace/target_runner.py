"""
This module is meant to wrap the GA code and provide a simpler
interface for Irace to run it. It's required to make parsing the parameter as
environment variables easie, which is a bit awkward to do purely via Irace scenario file.
"""

import logging
import pathlib
import subprocess

import argparse


UNFEASIBLE_SOLUTION_FLAG = 99999999999999
RESPONSE_FILE = pathlib.Path("heuristic_response.txt")
TIMEOUT = "600s"


def create_parser():
    parser = argparse.ArgumentParser(
        description="Parser for heuristic parameters (no defaults, values required)"
    )

    parser.add_argument(
        "--instance",
        type=str,
        help="Path to the instance file",
    )
    parser.add_argument("--seed", type=int, help="Random seed")
    parser.add_argument(
        "--ngen", type=int, help="Number of generations (range: 0-3000)"
    )

    parser.add_argument(
        "--qGenWithoutImprovement",
        type=int,
        help="Max generations without improvement (range: 0-1500)",
    )

    parser.add_argument("--psize", type=int, help="Population size (range: 0-4000)")

    parser.add_argument(
        "--pbetterParent",
        type=float,
        help="Probability of choosing better parent (range: 0.0-1.0)",
    )

    parser.add_argument(
        "--eliteFraction",
        type=float,
        help="Fraction of elite individuals (range: 0.0-1.0)",
    )

    parser.add_argument(
        "--mutationFraction",
        type=float,
        help="Fraction of individuals to mutate (range: 0.0-1.0)",
    )

    return parser


def parse_env_vars(args: argparse.Namespace) -> dict[str, str]:
    return {
        "ngen": str(args.ngen),
        "qGenWithoutImprovement": str(args.qGenWithoutImprovement),
        "psize": str(args.psize),
        "pbetterParent": str(args.pbetterParent),
        "eliteFraction": str(args.eliteFraction),
        "mutationFraction": str(args.mutationFraction),
    }


def main():
    parser = create_parser()
    args = parser.parse_args()

    r = subprocess.run(
        [
            "timeout",
            TIMEOUT,
            "java",
            "-jar",
            "target/ChallengeSBPO2025-1.0.jar",
            args.instance,
            str(RESPONSE_FILE.resolve()),
        ],
        env=parse_env_vars(args),
        check=False,
        capture_output=True,
    )
    if r.returncode != 0:
        print(UNFEASIBLE_SOLUTION_FLAG)
        return
    fo = r.stdout.decode().strip().split("\n")[0]
    print(f"-{fo}")


try:
    main()
except subprocess.CalledProcessError as e:
    logging.error("Warning: Fail to run heuristic: %s", e)
    # case an unexpected error occurs, so we'll conside that as a unfeasible test
    print(UNFEASIBLE_SOLUTION_FLAG)
