package com.example.jjjjjppppp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class TodoActivity : AppCompatActivity() {

    private lateinit var etTodo: EditText
    private lateinit var btnAdd: Button
    private lateinit var lvTodo: ListView
    private lateinit var tvEmpty: TextView

    private val todoItems = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private val PREFS_NAME = "TodoPrefs"
    private val KEY_TODOS = "todo_list"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        etTodo = findViewById(R.id.etTodo)
        btnAdd = findViewById(R.id.btnAdd)
        lvTodo = findViewById(R.id.lvTodo)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, todoItems)
        lvTodo.adapter = adapter

        loadTodos()

        btnAdd.setOnClickListener {
            addTodo()
        }

        lvTodo.setOnItemClickListener { _, _, position, _ ->
            showTodoOptions(position)
        }

        updateEmptyState()
    }

    private fun addTodo() {
        val todoText = etTodo.text.toString().trim()
        if (todoText.isNotEmpty()) {
            todoItems.add(todoText)
            adapter.notifyDataSetChanged()
            etTodo.text.clear()
            saveTodos()
            updateEmptyState()
        }
    }

    private fun showTodoOptions(position: Int) {
        val items = arrayOf("完成", "编辑", "删除")
        AlertDialog.Builder(this)
            .setTitle("选择操作")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> completeTodo(position)
                    1 -> editTodo(position)
                    2 -> deleteTodo(position)
                }
            }
            .show()
    }

    private fun completeTodo(position: Int) {
        val item = todoItems[position]
        if (!item.startsWith("✓ ")) {
            todoItems[position] = "✓ $item"
        } else {
            todoItems[position] = item.substring(2)
        }
        adapter.notifyDataSetChanged()
        saveTodos()
    }

    private fun editTodo(position: Int) {
        val editText = EditText(this)
        editText.setText(todoItems[position].removePrefix("✓ "))

        AlertDialog.Builder(this)
            .setTitle("编辑待办事项")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    todoItems[position] = newText
                    adapter.notifyDataSetChanged()
                    saveTodos()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteTodo(position: Int) {
        todoItems.removeAt(position)
        adapter.notifyDataSetChanged()
        saveTodos()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (todoItems.isEmpty()) {
            tvEmpty.visibility = android.view.View.VISIBLE
            lvTodo.visibility = android.view.View.GONE
        } else {
            tvEmpty.visibility = android.view.View.GONE
            lvTodo.visibility = android.view.View.VISIBLE
        }
    }

    private fun saveTodos() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_TODOS, todoItems.joinToString("|||"))
        editor.apply()
    }

    private fun loadTodos() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedTodos = prefs.getString(KEY_TODOS, "")
        if (!savedTodos.isNullOrEmpty()) {
            todoItems.clear()
            todoItems.addAll(savedTodos.split("|||"))
            adapter.notifyDataSetChanged()
        }
    }
}