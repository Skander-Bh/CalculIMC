package com.example.calculimc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var editTextPoids: EditText
    private lateinit var editTextTaille: EditText
    private lateinit var buttonCalculer: Button
    private lateinit var buttonEffacer: Button
    private lateinit var buttonPartager: Button
    private lateinit var textViewImc: TextView
    private lateinit var textViewCategorie: TextView

    // Dernier IMC calculé (null = aucun résultat affiché)
    private var imcCourant: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextPoids = findViewById(R.id.editTextPoids)
        editTextTaille = findViewById(R.id.editTextTaille)
        buttonCalculer = findViewById(R.id.buttonCalculer)
        buttonEffacer = findViewById(R.id.buttonEffacer)
        buttonPartager = findViewById(R.id.buttonPartager)
        textViewImc = findViewById(R.id.textViewImc)
        textViewCategorie = findViewById(R.id.textViewCategorie)

        buttonCalculer.setOnClickListener { calculer() }
        buttonEffacer.setOnClickListener { effacer() }
        buttonPartager.setOnClickListener { partager() }

        // Restauration du dernier résultat après une rotation de l'écran
        if (savedInstanceState != null && savedInstanceState.containsKey(CLE_IMC)) {
            afficherResultat(savedInstanceState.getDouble(CLE_IMC))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        imcCourant?.let { outState.putDouble(CLE_IMC, it) }
    }

    /** Lit un champ : renvoie le nombre, ou null en affichant une erreur dans le champ. */
    private fun lireValeur(champ: EditText): Double? {
        // Le clavier peut proposer une virgule : on la remplace par un point
        val texte = champ.text.toString().trim().replace(',', '.')

        if (texte.isEmpty()) {
            champ.error = getString(R.string.erreur_champ_vide)
            return null
        }

        val valeur = texte.toDoubleOrNull()
        if (valeur == null) {
            champ.error = getString(R.string.erreur_valeur_invalide)
            return null
        }

        if (valeur <= 0.0) {
            champ.error = getString(R.string.erreur_valeur_positive)
            return null
        }

        return valeur
    }

    private fun calculer() {
        val champsVides = editTextPoids.text.isBlank() || editTextTaille.text.isBlank()

        // Les deux champs sont toujours vérifiés (pour signaler toutes les erreurs)
        val poids = lireValeur(editTextPoids)
        val taille = lireValeur(editTextTaille)

        if (poids == null || taille == null) {
            // Aucun calcul : on retire l'ancien résultat et on prévient l'utilisateur
            masquerResultat()
            val message = if (champsVides) R.string.toast_completer else R.string.toast_erreur_saisie
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return
        }

        // IMC = poids / (taille × taille), arrondi à 2 chiffres après la virgule
        val imc = Math.round(poids / (taille * taille) * 100.0) / 100.0
        afficherResultat(imc)
    }

    /** Affiche la valeur, la catégorie et la couleur correspondant à l'IMC. */
    private fun afficherResultat(imc: Double) {
        imcCourant = imc

        val categorie: Int
        val couleur: Int

        if (imc < 18.5) {
            categorie = R.string.categorie_insuffisance
            couleur = R.color.imc_orange
        } else if (imc < 25.0) {
            categorie = R.string.categorie_normale
            couleur = R.color.imc_vert
        } else if (imc < 30.0) {
            categorie = R.string.categorie_surpoids
            couleur = R.color.imc_orange
        } else if (imc < 35.0) {
            categorie = R.string.categorie_obesite_moderee
            couleur = R.color.imc_rouge
        } else if (imc < 40.0) {
            categorie = R.string.categorie_obesite_severe
            couleur = R.color.imc_rouge
        } else {
            categorie = R.string.categorie_obesite_morbide
            couleur = R.color.imc_rouge_fonce
        }

        val couleurTexte = ContextCompat.getColor(this, couleur)
        textViewImc.text = getString(R.string.format_imc, imc)
        textViewImc.setTextColor(couleurTexte)
        textViewCategorie.setText(categorie)
        textViewCategorie.setTextColor(couleurTexte)
        buttonPartager.isEnabled = true
    }

    private fun masquerResultat() {
        imcCourant = null
        textViewImc.text = ""
        textViewCategorie.text = ""
        buttonPartager.isEnabled = false
    }

    /** Vide les champs, supprime le résultat et replace le curseur dans le poids. */
    private fun effacer() {
        editTextPoids.text.clear()
        editTextTaille.text.clear()
        editTextPoids.error = null
        editTextTaille.error = null
        masquerResultat()
        editTextPoids.requestFocus()
    }

    private fun partager() {
        val imc = imcCourant ?: return
        val texte = getString(R.string.partage_texte, imc, textViewCategorie.text)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texte)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.partage_titre)))
    }

    companion object {
        private const val CLE_IMC = "cle_imc"
    }
}
