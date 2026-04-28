package com.example.jjjjjppppp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jjjjjppppp.R

class SearchFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearch = view.findViewById(R.id.etSearch)
        btnSearch = view.findViewById(R.id.btnSearch)

        btnSearch.setOnClickListener {
            val searchTerm = etSearch.text.toString()
            if (searchTerm.isNotEmpty()) {
                Toast.makeText(requireContext(), "搜索: $searchTerm", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "请输入搜索内容", Toast.LENGTH_SHORT).show()
            }
        }
    }
}