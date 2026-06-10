# Vélib Metropole

Application Android permettant de consulter en temps réel les stations Vélib de Paris et sa région.

Ce README contient deux sections : l'installation du projet et le guide d'utilisation de l'application.

## Installation

### Prérequis

- Android Studio
- Android 8.0 (API 26) minimum
- Connexion internet pour charger les données en temps réel

### Lancer le projet

1. Cloner le dépôt
```
git clone https://github.com/minh-nguyen91/ProjetVelibSNI1_NGUYEN_Minh.git
```
2. Ouvrir le projet dans Android Studio
3. Lancer l'application sur un émulateur ou un téléphone Android

## Guide d'utilisation

### Carte

Au lancement, l'application affiche la carte OpenStreetMap centrée sur votre position avec toutes les stations Vélib.

Les marqueurs sont colorés selon la disponibilité. En mode Trouver un vélo ils indiquent le nombre de vélos disponibles, en mode Déposer un vélo ils indiquent le nombre de places libres :
- **Vert** : 5 ou plus
- **Orange** : moins de 5
- **Rouge** : aucun
- **Gris** : station hors service

Plusieurs stations proches sont regroupées en un seul marqueur bleu affichant le total. Appuyez dessus pour zoomer et les séparer.

Appuyez sur un marqueur pour voir le nom de la station, le nombre de vélos mécaniques et électriques, et la distance depuis votre position. Deux boutons permettent d'obtenir un itinéraire vers la station ou d'ouvrir sa fiche détaillée.

Les boutons en haut à droite permettent de rechercher une station par nom, de recentrer la carte sur votre position et de zoomer. Les boutons en bas permettent de basculer entre le mode Trouver un vélo et Déposer un vélo, et de masquer ou afficher les marqueurs.

### Stations proches

Cet onglet liste les stations disponibles autour de vous.

- Ajustez le rayon de recherche avec le curseur (250 m à 5 km)
- Filtrez par type de vélo : tous, mécanique ou électrique
- Basculez en mode Déposer pour voir les stations avec des places libres
- Appuyez sur une station pour ouvrir sa fiche détaillée
- Appuyez sur l'étoile pour ajouter ou retirer la station de vos favoris

### Favoris

Cet onglet affiche les stations que vous avez sauvegardées. Elles sont stockées localement sur votre téléphone et restent accessibles même sans connexion internet. Appuyez sur l'étoile pour retirer une station de vos favoris.

### Fiche détaillée

La fiche d'une station affiche le nombre de vélos disponibles, le nombre de places libres, le détail par type de vélo (mécanique et électrique), la capacité totale, le statut de la station, l'heure de dernière mise à jour et la distance depuis votre position.

Le bouton Itinéraire ouvre Google Maps avec l'itinéraire à pied vers la station. L'étoile en bas à droite permet d'ajouter ou retirer la station de vos favoris.

## Données

Les données proviennent de l'API officielle Vélib Metropole.
