package com.example.scan.viewModel

import androidx.lifecycle.ViewModel
import com.example.scan.model.data.entiti.Empresa
import com.example.scan.model.data.entiti.Sucursal
import com.example.scan.model.retrofit.fetchListEmpresa
import com.example.scan.model.retrofit.fetchListSucursal


class ObtenerEmpresa_Sucursa_lViewModel() : ViewModel() {
    fun obtenerListaEmpresa(empresas: (List<String>) -> Unit = {}) {
        fetchListEmpresa() { empresas ->
         //   empresas(empresas)
        }

    }
    fun ontenerListaSucursal(idempresa:String,sucursal: (List<String>) -> Unit = {}){
        fetchListSucursal(idempresa){
            sucursal->sucursal(sucursal)

        }
    }

}