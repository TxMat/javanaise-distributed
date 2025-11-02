# Utilisation

Les commandes doivent être exécutées depuis le dossier /scripts et le Coordinateur doit être lancé avant les serveurs.

### 1. Compilation des fichiers
```
./compil
```

### 2. Lancement du coordinateur :
```
./coord
```

### 3. Lancement d'un serveur local
```
./server
```

### 4. Pour tester avec 2 serveurs

Le mieux est d'avoir 3 terminaux pour : 
- le Coordinateur
- le Serveur 1
- le Serveur 2

Commandes recommandées : 
#### > coord
```
clear && ./compil && ./coord
```
#### > server
```
./server
```

# Test :

## 1. Créer/Lookup des objets classiques

Pour créer des objets il existe deux commandes : 

### A. Créer un nouvel objet
Pour créer un nouvel objet classique :
```
create <type> <jon> [value]
```
Avec comme type disponible : `A` et `S`  
`jon` le nom de l'objet  
Éventuellement une valeur précise

### B. Récupérer un objet existant 
Pour récupérer un objet existant sur le Coordinateur :
```
lookup <type> <jon>
```
Avec comme type disponible : `A`, `S`, `S3` et `SM`  
`jon` le nom de l'objet à récupérer

## 2. Multi-Répartis Object (MRO)

Pour tester les objets multi-répartis il existe la possibilité d'utiliser le `mro` et la commande `mro`  
Le MRO mis à disposition contient une Map<String, JvnObject> avec 3 fonctions : Ajouter, Retirer et Obtenir un élément de la Map.

Aide possible depuis la console : 
```
mro [help]
```

#### A. Créer ou récupérer le MRO existant :
```
mro init
```
#### B. Ajouter / Retirer des JvnObject du MRO :

Ajouter un nouveau JvnObject :
```
mro add <jon>
```
avec `jon` le nom du JvnObject à créer, il ne doit pas exister un autre JvnObject avec ce nom

Retirer un JvnObject du MRO :
```
mro remove <jon>
```

#### C. Action sur un JvnObject dans le MRO : 

Lecture de l'objet : 
```
mro <jon> r
```
- `r` = READ

MAJ de la valeur (SET) :
```
mro <jon> s <value>
```
- `s` = SET

MAJ de la valeur (ADD) :
```
mro <jon> a <value>
```
- `a` = ADD

## 3. Stress Test :

#### A. Explications :

Le principe est de lancer plusieurs serveurs qui vont constamment augmenter un compteur commun afin d'essayer de provoquer des deadlocks, erreurs, incohérences du compteur, ...

#### B. Lancer : Coordinateur & 2 serveurs ou plus
Dans le dossier `scripts` :
```
./coord
```
```
./server
```
#### C. Sur les serveurs effectuez la commande suivante :
```
cpt <nb>
```
Le 1er serveur à utiliser cette commande (donc celui qui va créer l'objet original `A cpt = JvnInterceptor.createInterceptor(...);`) laissera un délai de 5 secondes avant de commencer à "compter"

## 4. JvnObject Cyclique (non fonctionnel mais avancé)

#### A. Configuration initiale :

Le coordinateur + 2 serveurs au moins de lancés  
Sur le coordinateur et les serveurs, activer toutes les logs pour voir ce qu'il se passe : 
```
print_all y
```

#### B. Créer le cycle : 

Sur un serveur : 
```
cycle
```

Sur le Coordinateur :  
Afficher comment est représenté le cycle : 
```
ls
```

#### C. Récupérer le cycle sur le deuxième serveur :
```
lookup S3 cycle
```

## 5. SM (Serializable Map)

=> Ressemble aux MRO mais il y a des commandes plus poussées

#### A. Créer un SM
```
sm new <jon> <type>
```
Créer un nouveau SM avec le nom `<jon>` du type : A, S, S3

On peut lister les SM avec : 
```
sm ls
```
Ou afficher les détails d'un SM avec :
```
sm <smn>
```
- avec smn le SM name

#### B. Ajouter un JvnObject à un SM : 
Ajouter un JvnObject (existant ou en créer un) à un SM.
```
sm addto <smn> [?new] <type> <jon>
```

#### C. Appel d'une méthode sur un Object dans le SM :
```
sm meth <smn> <jon> <type> <method> [param]
```
Appelle une méthode sur un JON contenu dans le SM indiqué  
Méthodes pour le type : 
- A 
  - `addValue [value | 10]`
  - `setValue [value | 0]`
  - `getValue`
- S1
  - `addValue [value | 10]`
  - `setValue [value | 0]`
  - `getValue`
- S3 
  - `setObj <jon>`
  - `getObj`
  - `toString`

#### D. Auto : 

```
sm auto
```

Créer automatiquement :
- Un SM de A
  - `sm_a` avec les objets : `_a0` et `_a1`
- Un SM de S (S1)
  - `sm_s1` avec les objets : `_s0` et `_s1`
- Un SM de S3 `sm_s3` avec les objets suivants :
  - `_s3_s0` { null }
  - `_s3_s1` { null }
  - `_s3_a0` {  `a` }
  - `_s3_a1` { null }

Après cela on peut utiliser : 
```
sm auto call
```
pour accéder au `a` de : <br>
`sm_s3.get(_sm_a0).get()` <br>
`sm_s3 -> _sm_a0 -> a`
