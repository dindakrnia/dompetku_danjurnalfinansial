package com.example.dompetkujurnalfinansial

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvPengguna: TextView
    private lateinit var tvStatusMember: TextView
    private lateinit var tvPoinRewards: TextView
    private lateinit var tvTotalSaldo: TextView
    private lateinit var tvTargetAnggaran: TextView
    private lateinit var tvPersenProgres: TextView

    private lateinit var etNamaPengguna: EditText
    private lateinit var etTargetUang: EditText
    private lateinit var etNominalTransaksi: EditText

    private lateinit var btnSimpanProfil: Button
    private lateinit var btnTambahSaldo: Button
    private lateinit var btnKuerangSaldo: Button
    private lateinit var btnModeTerang: Button
    private lateinit var btnModeGelap: Button
    private lateinit var btnResetSemua: Button

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREF_NAME = "LuxPayPremiumPref"
        private const val KEY_NAMA = "KEY_NAMA"
        private const val KEY_TARGET = "KEY_TARGET"
        private const val KEY_SALDO = "KEY_SALDO"
        private const val KEY_POIN = "KEY_POIN"
        private const val KEY_THEME = "KEY_THEME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Muat preferensi tema sebelum tampilan dibuat
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(KEY_THEME, true)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)

        tvPengguna = findViewById(R.id.tvPengguna)
        tvStatusMember = findViewById(R.id.tvStatusMember)
        tvPoinRewards = findViewById(R.id.tvPoinRewards)
        tvTotalSaldo = findViewById(R.id.tvTotalSaldo)
        tvTargetAnggaran = findViewById(R.id.tvTargetAnggaran)
        tvPersenProgres = findViewById(R.id.tvPersenProgres)

        etNamaPengguna = findViewById(R.id.etNamaPengguna)
        etTargetUang = findViewById(R.id.etTargetUang)
        etNominalTransaksi = findViewById(R.id.etNominalTransaksi)

        btnSimpanProfil = findViewById(R.id.btnSimpanProfil)
        btnTambahSaldo = findViewById(R.id.btnTambahSaldo)
        btnKuerangSaldo = findViewById(R.id.btnKuerangSaldo)
        btnModeTerang = findViewById(R.id.btnModeTerang)
        btnModeGelap = findViewById(R.id.btnModeGelap)
        btnResetSemua = findViewById(R.id.btnResetSemua)

        aturFormatRupiah(etTargetUang)
        aturFormatRupiah(etNominalTransaksi)

        muatDataAplikasi()

        btnSimpanProfil.setOnClickListener {
            val nama = etNamaPengguna.text.toString().trim()
            val targetStr = bersihkanFormatRupiah(etTargetUang.text.toString())

            if (nama.isEmpty() || targetStr.isEmpty()) {
                Toast.makeText(this, "Silakan lengkapi nama dan target tabungan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val target = targetStr.toFloatOrNull() ?: 0f

            val editor = sharedPreferences.edit()
            editor.putString(KEY_NAMA, nama)
            editor.putFloat(KEY_TARGET, target)
            editor.apply()

            Toast.makeText(this, "Profil & Target Tabungan diperbarui!", Toast.LENGTH_SHORT).show()
            muatDataAplikasi()
        }

        btnTambahSaldo.setOnClickListener {
            eksekusiTransaksi(isTambah = true)
        }

        btnKuerangSaldo.setOnClickListener {
            eksekusiTransaksi(isTambah = false)
        }

        btnModeTerang.setOnClickListener {
            sharedPreferences.edit().putBoolean(KEY_THEME, false).apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate()
        }

        btnModeGelap.setOnClickListener {
            sharedPreferences.edit().putBoolean(KEY_THEME, true).apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()
        }

        btnResetSemua.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            etNamaPengguna.text.clear()
            etTargetUang.text.clear()
            etNominalTransaksi.text.clear()
            Toast.makeText(this, "Seluruh data berhasil direset!", Toast.LENGTH_SHORT).show()
            muatDataAplikasi()
        }
    }

    override fun onResume() {
        super.onResume()
        muatDataAplikasi()
    }

    private fun eksekusiTransaksi(isTambah: Boolean) {
        val inputNominal = bersihkanFormatRupiah(etNominalTransaksi.text.toString())
        if (inputNominal.isEmpty()) {
            Toast.makeText(this, "Masukkan nominal transaksi terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        val nominal = inputNominal.toFloatOrNull() ?: 0f
        val saldoLama = sharedPreferences.getFloat(KEY_SALDO, 0f)
        var poinLama = sharedPreferences.getInt(KEY_POIN, 0)

        val saldoBaru = if (isTambah) {
            poinLama += 50
            saldoLama + nominal
        } else {
            if (nominal > saldoLama) {
                Toast.makeText(this, "Saldo dompet tidak mencukupi!", Toast.LENGTH_SHORT).show()
                return
            }
            saldoLama - nominal
        }

        val editor = sharedPreferences.edit()
        editor.putFloat(KEY_SALDO, saldoBaru)
        editor.putInt(KEY_POIN, poinLama)
        editor.apply()

        etNominalTransaksi.text.clear()
        Toast.makeText(this, "Transaksi Berhasil Dicatat!", Toast.LENGTH_SHORT).show()
        muatDataAplikasi()
    }

    private fun muatDataAplikasi() {
        val nama = sharedPreferences.getString(KEY_NAMA, "Pengguna") ?: "Pengguna"
        val target = sharedPreferences.getFloat(KEY_TARGET, 0f)
        val saldo = sharedPreferences.getFloat(KEY_SALDO, 0f)
        val poin = sharedPreferences.getInt(KEY_POIN, 0)

        tvPengguna.text = "Halo, $nama"
        tvTotalSaldo.text = formatRupiah(saldo.toDouble())
        tvTargetAnggaran.text = formatRupiah(target.toDouble())
        tvPoinRewards.text = "💎 $poin Poin"

        if (target > 0) {
            val persen = ((saldo / target) * 100).toInt()
            tvPersenProgres.text = "$persen%"
        } else {
            tvPersenProgres.text = "0%"
        }
    }

    private fun formatRupiah(nominal: Double): String {
        val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatRp.format(nominal)
    }

    private fun bersihkanFormatRupiah(input: String): String {
        return input.replace(".", "").replace("Rp", "").trim()
    }

    private fun aturFormatRupiah(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var sedangMengubah = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (sedangMengubah) return
                sedangMengubah = true

                val stringAsli = s.toString().replace(".", "")
                if (stringAsli.isNotEmpty()) {
                    val angka = stringAsli.toLongOrNull() ?: 0L
                    val formatAngka = NumberFormat.getNumberInstance(Locale("id", "ID")).format(angka)
                    editText.setText(formatAngka)
                    editText.setSelection(formatAngka.length)
                }

                sedangMengubah = false
            }
        })
    }
}