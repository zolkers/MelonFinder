---

## Code Rules (enforce on every task)

- Methods: cyclomatic complexity ≤ 15
- Loops: max 1 break or continue per for loop
- Fields: always private, getters only
- Qualifiers: fully qualified names only in imports
- `var`: never — always explicit types
- Registries: preferred over switch/if chains for extensible concerns
- No magic: no reflection scanning, no annotation processors, all registration is explicit

---