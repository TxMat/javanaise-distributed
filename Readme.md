# Important :
Lors des create et des lookup, il faut préciser le type de l'objet. Voire la partie **Test - 1.**

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

#### Logs : 
On peut afficher les logs "poussé" : serialisation / déserialisation custom, appel des Lock, Unlock, ... et autre avec la commande suivante :
```
print_all <y/n>
```
Par défaut à "no"
Bien sur ce parametre est propre a chanque machine (l'activer sur le Server 2 ne l'active pas sur le Server 1 et le COordinateur par exemple).

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
Pour créer des S3, voir la partie ** Test - 6.**
Pour créer un MRO, voir la partie ** Test - 2.**
Pour créer des SM, voir la partie ** Test - 5.**
Pour le cycle, voir la partie ** Test - 4.**

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
- `r` = READ ( Va l'afficher dans la console en utilisant @JvnAnnotation(READ) getValue() )

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

#### D. Test conseillé : 

- Utilisation d'un Coordinateur et 2 Servers

Sur le server 1 : Créer le MRO et ajouter 2 objets (éventuelement ls pour voir que le MRO exique bien avec 2 objets) et afficher la valeur de `mro_a0` pour la suite
- Server 1
```
mro init
mro add mro_a0
mro add mro_a1
ls
mro mro_a0 r 
```
Sur le Coordinateur on peut bien voir que les 3 objet sont répartis : 
- Coord
```
ls
```
Sur le server 2 on peut récupérer le JvnO `mro_a0` seul, modifier ca valeur et vérifier qu'elle est bien modifier dans le MRO du Server 1 :
- Server 2 : 
```
lookup A mro_a0
test mro_a0 <value>
```
pour rappel : `test <jon> <value>` ajoute `<value>` à l'objet `<jon>` qui doit être une instance de class `A` (vien du main, n'a pas été modifier, c'est codé comme ca pour le simplifier)
- Server 1 :
Vérifier que la valeur a bien été modifié : 
```
mro mro_a0 r
```

Pour la suite, on va récupérer le MRO sur le Server 2 et ajouter un objet: 
- Server 2
```
mro init
mro add mro_a3
```
Sur le Server 1, en cas de `ls` il affichera toujours 2 objet car les toString ne mettent pas a jour les JvnObject mais on peut accéder au JvnO `mro_a3` et le modifier en fesant un SET à 0 par exemple :
- Server 1 
```
ls
mro mro_a3 s 0
```

Aussi on peut remove un objet sur le Server 1 par exemple :
- Server 1
```
mro remove mro_a1
```
On ne peut plus modifier le JvnO `mro_a1` depuis le MRO (on peut toujours tester) mais le mro_a1 existe toujours sur le coordinateur et on peut le lookup individuelement (comportement normal et prévu)
- Server 2
```
mro mro_a1 add 20
ls
lookup A mro_a1
ls
test mro_a1 20
```


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
Le 1er serveur à utiliser cette commande (donc celui qui va créer l'objet original `A cpt = JvnInterceptor.createInterceptor(...);`) laissera un délai de 5 secondes avant de commencer à "compter".
Les autres font que récupérer l'objet et lance directement l'incrémentation.
Il est donc conseillé de lancer le `cpt` du Server 2 en même temps que la fin du compteur du Server 1.

Si toutes les logs sont afficher (pour voir les lock, unlock et les actions) avec la commande suivante :
```
print_all y
```
Le <nb> conseillé est de 25000 (sur le 1er server a finir sa valeur ne sera pas de 50000 car il n'aura pas "lut" la dernière valeur) mais sur le 2eme on verra bien la valeur de 50000 (pour vérifier que le cpt est bien a 50000, on peut lui ajouter 0 sur le server qui a fini en 1er avec `test cpt 0` ou `lookup A cpt` car le `ls` utilise le `toString` qui ne demande pas de ReadLock et donc ne va pas chercher la dernière valeur)

Sinon il est conseillé d'utiliser 1 000 000 à 2 500 000 sans les logs 
```
print_all n
```
Par défaut le print_all est a `false` / `no`

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
Ne jamais `ls` ici car c'est un cycle réel, le toString ne le gère pas et donc créé un StackOverflow

Sur le Coordinateur :  
Afficher comment est représenté le cycle : 
```
ls
```

#### C. Récupérer le cycle sur le deuxième serveur :
```
lookup S3 cycle
ls
```
on a : 
`cycle = S3_Impl { S3_Impl { S3_Impl { S3_Impl { S3_Impl { SerializedInterceptor:{ jon: _s3_2 } } } } } }`
ou de manière plus lisible : 
`cycle { _s3_s2 { _s3_s3 { _s3_s4 { cycle { SerializedInterceptor(_s3_s2) } } } } }`
Ce n'est donc pas un reel cycle, il reste plus qu'a réussir a faire la réel boucle

Il est conseillé de relancer le Server qui a créé le cycle car cela empeche d'utiliser un print sur l'objet sous peine d'erreur

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

#### D. Récupérer un SM sur un autre server :

```
lookup SM <jon>
```

#### E. Auto : 

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

## 6. Test des S3 (Serializable qui contien un Serializable)

#### A. créer un S3 :
```
test2 create <jon> <type>
```
avec comme type A ou S
(Ces contraintes sont uniquement pour simplifier les "interaction" en ligne de commande, sinon il faut directement coder en brute une fonction et rebuild)
#### B. metre un JvnO dans le S3 :
l'objet doit exister en local (create ou lookup)
```
create <A/S> <jon>
```
ou 
```
lookup <A/S> <jon>
```
Mettre l'objet dans le S3 :
```
test2 set <jon> in <s3 jon>
```
#### C. Appeler une methode sur l'objet sous le S3

```
test2 meth <meth name> under <s3 name>
```
avec `<meth name>` : 
- `add`
- `set`
- `get`
`get` affiche la valeur de l'objet en utilisant getValue() donc récupère la dernière valeur

#### D. Récupérer le S3 sur un autre server :

```
lookup S3 <s3 name>
```

#### E. Auto :

Créer un `A`, un `S`, un `S3<A>` et un `S3<S>`.
Modifit la valeur du `A` sous le `S3<A>` et de même pour le `S3<S>`