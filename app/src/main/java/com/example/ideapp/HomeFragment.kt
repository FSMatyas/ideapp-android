package com.example.ideapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.adapter.IdeaAdapter
import com.example.ideapp.data.Idea
import com.example.ideapp.viewmodel.IdeaViewModel
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.min

class HomeFragment : Fragment() {
    
    private val viewModel: IdeaViewModel by viewModels()
    private lateinit var ideaAdapter: IdeaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var paginationContainer: ViewGroup

    private var currentPage = 0
    private val pageSize = 5
    private var allIdeas: List<Idea> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecyclerView()
        paginationContainer = view.findViewById(R.id.paginationContainer)
        observeViewModel()

        // --- In-app admin message notification listener ---
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val deviceId = com.example.ideapp.util.DeviceIdUtil.getDeviceId(requireContext())
        db.collection("ideas")
            .whereEqualTo("creatorId", deviceId)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                if (!isAdded) return@addSnapshotListener
                val ctx = context ?: return@addSnapshotListener

                for (docChange in snapshots.documentChanges) {
                    val idea = docChange.document.toObject(com.example.ideapp.data.Idea::class.java)
                    val adminMessages = idea.messages.filter { it.sender == "admin" }
                    val prefs = ctx.getSharedPreferences("admin_msg_prefs", android.content.Context.MODE_PRIVATE)
                    val lastSeenCount = prefs.getInt("admin_msg_count_${idea.id}", 0)
                    if (adminMessages.size > lastSeenCount) {
                        // --- Show real notification ---
                        val channelId = "admin_message_channel_v2" // New channel ID to force recreation
                        val notificationId = idea.id.hashCode()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val name = "Admin üzenetek"
                            val descriptionText = "Értesítések admin üzenetekről"
                            val importance = NotificationManager.IMPORTANCE_HIGH
                            val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                            val channel = NotificationChannel(channelId, name, importance).apply {
                                description = descriptionText
                                enableVibration(true)
                                setSound(soundUri, android.app.Notification.AUDIO_ATTRIBUTES_DEFAULT)
                            }
                            val notificationManager: NotificationManager = ctx.getSystemService(NotificationManager::class.java)
                            notificationManager.createNotificationChannel(channel)
                        }
                        val intent = requireActivity().intent.clone() as android.content.Intent
                        intent.putExtra("open_idea_id", idea.id)
                        val pendingIntent = android.app.PendingIntent.getActivity(
                            ctx,
                            0,
                            intent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        val builder = NotificationCompat.Builder(ctx, channelId)
                            .setSmallIcon(android.R.drawable.ic_dialog_email)
                            .setContentTitle("Új admin üzenet")
                            .setContentText(adminMessages.last().text)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .setColor(resources.getColor(R.color.primary_color, null))
                            .setContentIntent(pendingIntent)
                        with(NotificationManagerCompat.from(ctx)) {
                            notify(notificationId, builder.build())
                        }
                        prefs.edit().putInt("admin_msg_count_${idea.id}", adminMessages.size).apply()
                    }
                }
            }
    }
    
    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewIdeas)
        progressBar = view.findViewById(R.id.progressBar)

        val submitButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSubmitIdea)
        submitButton.setOnClickListener {
            val dialog = com.example.ideapp.SubmitIdeaDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "SubmitIdeaDialog")
        }

        val bulbIcon = view.findViewById<android.widget.ImageView>(R.id.bulbIcon)
        bulbIcon.setOnLongClickListener {
            // --- ADMIN LOGIN: Új Activity indítása ---
            // Ezt a blokkot bármikor eltávolíthatod vagy módosíthatod.
            val intent = android.content.Intent(requireContext(), AdminLoginActivity::class.java)
            startActivity(intent)
            true
        }
    }
    
    private fun updateIdeaStatus(ideaId: String, newStatus: com.example.ideapp.data.IdeaStatus) {
        allIdeas = allIdeas.map { idea ->
            if (idea.id == ideaId) idea.copy(status = newStatus) else idea
        }
    }

    private fun setupRecyclerView() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        ideaAdapter = IdeaAdapter(
            onIdeaClick = { idea -> onIdeaClick(idea) },
            blurUnapproved = true,
            currentUserEmail = currentUserEmail,
            onSendReply = { idea, reply ->
                com.example.ideapp.repository.IdeaRepository.sendUserReply(idea.id, reply,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Válasz elküldve!", Toast.LENGTH_SHORT).show()
                        // Optionally refresh ideas here
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Hiba: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                )
            },
            onStatusChanged = {
                // Remove all ideas with status IN_DEVELOPMENT from the landing page immediately
                allIdeas = allIdeas.filter { it.status != com.example.ideapp.data.IdeaStatus.IN_DEVELOPMENT }
                updatePagedIdeas()
                updatePaginationButtons()
            }
        )
        
        recyclerView.apply {
            adapter = ideaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun observeViewModel() {
        // Observe approved ideas
        viewModel.ideas.observe(viewLifecycleOwner) { ideas ->
            // Only show ideas with status PENDING or APPROVED
            allIdeas = ideas
                .filter { it.status == com.example.ideapp.data.IdeaStatus.PENDING || it.status == com.example.ideapp.data.IdeaStatus.APPROVED }
                .sortedByDescending { it.createdAt?.toDate() }
            updatePagedIdeas()
            updatePaginationButtons()
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
        
        // Observe submit success
        viewModel.submitSuccess.observe(viewLifecycleOwner) { success ->
            success?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearSubmitSuccess()
            }
        }
    }
    
    private fun onIdeaClick(idea: Idea) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_idea_detail, null)

        // Set data into dialog views
        dialogView.findViewById<TextView>(R.id.tvDialogTitle).text = idea.title
        dialogView.findViewById<TextView>(R.id.tvDialogCategory).text = getCategoryIcon(idea.category) + " " + idea.category.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
        }
        dialogView.findViewById<TextView>(R.id.tvDialogSubmitter).text = "by ${idea.submitterName}"
        val dateText = idea.createdAt?.toDate()?.let {
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(it)
        } ?: "Just now"
        dialogView.findViewById<TextView>(R.id.tvDialogDate).text = dateText
        dialogView.findViewById<TextView>(R.id.tvDialogDescription).text = idea.description

        // Show messages in the dialog at the bottom
        val layoutDialogMessages = dialogView.findViewById<android.widget.LinearLayout>(R.id.layoutDialogMessages)
        layoutDialogMessages?.removeAllViews()
        if (idea.messages.isNotEmpty()) {
            idea.messages.forEach { msg ->
                val tv = android.widget.TextView(requireContext())
                tv.text = if (msg.sender == "admin") {
                    "Admin: ${msg.text}"
                } else {
                    "Te: ${msg.text}"
                }
                tv.setPadding(0, 4, 0, 4)
                layoutDialogMessages?.addView(tv)
            }
        }

        // --- Reply box logic ---
        val layoutReply = dialogView.findViewById<android.widget.LinearLayout>(R.id.layoutReplyDialog)
        val etUserReply = dialogView.findViewById<android.widget.EditText>(R.id.etUserReplyDialog)
        val btnSendReply = dialogView.findViewById<android.widget.Button>(R.id.btnSendReplyDialog)
        layoutReply?.visibility = View.GONE
        // Only show if current user is creator and there is at least one admin message
        val deviceId = com.example.ideapp.util.DeviceIdUtil.getDeviceId(requireContext())
        val hasAdminMsg = idea.messages.any { it.sender == "admin" }
        if (deviceId == idea.creatorId && hasAdminMsg) {
            layoutReply?.visibility = View.VISIBLE
            btnSendReply?.setOnClickListener {
                val replyText = etUserReply?.text?.toString()?.trim()
                if (!replyText.isNullOrEmpty()) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val message = hashMapOf(
                        "sender" to deviceId,
                        "text" to replyText,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                    db.collection("ideas")
                        .document(idea.id)
                        .update("messages", com.google.firebase.firestore.FieldValue.arrayUnion(message))
                    layoutReply.visibility = View.GONE
                    Toast.makeText(requireContext(), "Válasz elküldve!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        // Make dialog background transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    // Helper for category icon (moved from IdeaAdapter)
    private fun getCategoryIcon(category: String): String {
        return when (category.lowercase()) {
            "productivity" -> "\uD83D\uDCBC" // 💼
            "health" -> "\uD83C\uDFE5" // 🏥
            "education" -> "\uD83C\uDF93" // 🎓
            "social" -> "\uD83D\uDC65" // 👥
            "entertainment" -> "\uD83C\uDFAC" // 🎬
            "utility" -> "\uD83D\uDD27" // 🔧
            "finance" -> "\uD83D\uDCB0" // 💰
            "other" -> "\uD83D\uDCF1" // 📱
            else -> "\uD83D\uDCF1" // 📱
        }
    }

    private fun updatePagedIdeas() {
        val fromIndex = currentPage * pageSize
        val toIndex = min(fromIndex + pageSize, allIdeas.size)
        val pagedIdeas = if (fromIndex < toIndex) allIdeas.subList(fromIndex, toIndex) else emptyList()
        ideaAdapter.submitList(pagedIdeas)
    }

    private fun updatePaginationButtons() {
        paginationContainer.removeAllViews()
        val pageCount = (allIdeas.size + pageSize - 1) / pageSize
        for (i in 0 until pageCount) {
            val button = android.widget.TextView(requireContext())
            button.text = (i + 1).toString()
            button.setTextColor(android.graphics.Color.WHITE)
            button.textSize = 16f
            button.gravity = android.view.Gravity.CENTER
            button.setPadding(0, 0, 0, 0)
            val size = resources.displayMetrics.density * 36 // 36dp
            val params = ViewGroup.MarginLayoutParams(size.toInt(), size.toInt())
            params.setMargins(12, 0, 12, 0)
            button.layoutParams = params
            button.background = if (i == currentPage)
                requireContext().getDrawable(R.drawable.pagination_circle_selected)
            else
                requireContext().getDrawable(R.drawable.pagination_circle)
            button.setOnClickListener {
                currentPage = i
                updatePagedIdeas()
                updatePaginationButtons()
            }
            if (i == currentPage) {
                button.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            paginationContainer.addView(button)
        }
    }

    fun openIdeaById(ideaId: String) {
        val idea = allIdeas.find { it.id == ideaId }
        if (idea != null) {
            onIdeaClick(idea)
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
