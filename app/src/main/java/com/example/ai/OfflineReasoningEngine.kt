package com.example.ai

import com.example.data.model.ActionProposal
import com.example.data.model.ActionType
import java.util.Locale

class OfflineReasoningEngine {

    data class OfflineResult(
        val reply: String,
        val proposal: ActionProposal? = null
    )

    fun processCommand(
        input: String,
        personality: String,
        contextMemories: List<String> = emptyList()
    ): OfflineResult {
        val trimmed = input.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Check for Memory commands
        val memoryPrefixBn = listOf("এটা মনে রাখো", "মনে রাখো", "মেমোরিতে রাখো", "মনে রাখ")
        val memoryPrefixEn = listOf("remember that", "remember this", "remember:", "remember")

        for (prefix in memoryPrefixBn) {
            if (lower.startsWith(prefix)) {
                val content = trimmed.substring(prefix.length).trim().removePrefix(":").trim()
                if (content.isNotEmpty()) {
                    return OfflineResult(
                        reply = "আমি কি এই তথ্যটি আপনার ব্যক্তিগত মেমোরিতে সংরক্ষণ করব? আপনার নিশ্চিতকরণ প্রয়োজন।",
                        proposal = ActionProposal(
                            type = ActionType.SAVE_MEMORY,
                            title = "মেমোরিতে সংরক্ষণ",
                            description = "\"$content\"",
                            payload = content
                        )
                    )
                }
            }
        }

        for (prefix in memoryPrefixEn) {
            if (lower.startsWith(prefix)) {
                val content = trimmed.substring(prefix.length).trim().removePrefix(":").trim()
                if (content.isNotEmpty()) {
                    return OfflineResult(
                        reply = "Would you like me to store this in your private local memory? Please confirm.",
                        proposal = ActionProposal(
                            type = ActionType.SAVE_MEMORY,
                            title = "Save to Memory",
                            description = "\"$content\"",
                            payload = content
                        )
                    )
                }
            }
        }

        // 2. Sensitive Action Detection (Strict Rule: NEVER perform without explicit user confirmation!)
        // Call command
        if (lower.contains("কল করো") || lower.contains("ফোন করো") || lower.startsWith("call ")) {
            val target = trimmed.replace("কল করো", "").replace("ফোন করো", "").replace("call", "").trim()
            return OfflineResult(
                reply = "আপনি কি নিশ্চিত যে $target নম্বরে কল করতে চান? আপনার স্পষ্ট অনুমতি ছাড়া আমি কোনো কল করব না।",
                proposal = ActionProposal(
                    type = ActionType.CALL,
                    title = "ফোন কল / Phone Call",
                    description = "নম্বর বা যোগাযোগ: $target",
                    payload = target
                )
            )
        }

        // SMS command
        if (lower.contains("এসএমএস") || lower.contains("মেসেজ পাঠাও") || lower.startsWith("send sms") || lower.startsWith("text ")) {
            return OfflineResult(
                reply = "আপনি কি নিশ্চিত যে এই বার্তাটি পাঠাতে চান? আপনার অনুমতি ছাড়া কোনো বার্তা পাঠানো হবে না।",
                proposal = ActionProposal(
                    type = ActionType.SMS,
                    title = "এসএমএস প্রেরণ / Send SMS",
                    description = trimmed,
                    payload = trimmed
                )
            )
        }

        // Open App command
        if (lower.contains("অ্যাপ খোলো") || lower.contains("ওপেন করো") || lower.startsWith("open app") || lower.startsWith("open ")) {
            val appName = trimmed.replace("অ্যাপ খোলো", "").replace("ওপেন করো", "").replace("open app", "").replace("open", "").trim()
            return OfflineResult(
                reply = "আপনি কি $appName অ্যাপ্লিকেশনটি খুলতে চান? নিশ্চিত করুন।",
                proposal = ActionProposal(
                    type = ActionType.OPEN_APP,
                    title = "অ্যাপ খোলা / Open App",
                    description = "অ্যাপ্লিকেশন: $appName",
                    payload = appName
                )
            )
        }

        // Delete command
        if (lower.contains("মুছে ফেলো") || lower.contains("ডিলিট করো") || lower.contains("delete") || lower.contains("clear")) {
            return OfflineResult(
                reply = "সতর্কতা: এটি একটি ডেটা মুছে ফেলার নির্দেশ। আপনার দ্বিমুখী নিশ্চিতকরণ ছাড়া কোনো কিছু মোছা হবে না। আপনি কি এগিয়ে যেতে চান?",
                proposal = ActionProposal(
                    type = ActionType.DELETE_DATA,
                    title = "ডেটা মোছা / Delete Data",
                    description = trimmed,
                    payload = trimmed
                )
            )
        }

        // Settings change command
        if (lower.contains("সেটিংস পরিবর্তন") || lower.contains("change setting") || lower.contains("modify setting")) {
            return OfflineResult(
                reply = "সেটিংস পরিবর্তনের জন্য আপনার সুস্পষ্ট অনুমোদন দরকার। আপনি কি এই পরিবর্তন অনুমোদন করছেন?",
                proposal = ActionProposal(
                    type = ActionType.SETTINGS_CHANGE,
                    title = "সেটিংস পরিবর্তন / Settings Change",
                    description = trimmed,
                    payload = trimmed
                )
            )
        }

        // 3. Identity and Help questions
        if (lower.contains("তুমি কে") || lower.contains("who are you") || lower.contains("what are you") || lower.contains("পরিচয়")) {
            return OfflineResult(
                reply = "আমি আপনার ব্যক্তিগত AI Assistant। একজন সিনিয়র সফটওয়্যার ইঞ্জিনিয়ার ও সিস্টেম আর্কিটেক্টের সমমানের জ্ঞান দিয়ে আমি সজ্জিত। আপনার স্পষ্ট অনুমতি ছাড়া আমি কখনো নিজে থেকে কোনো কাজ বা পরিবর্তন করব না। অফলাইন ও অনলাইন উভয় মোডেই আমি প্রস্তুত।"
            )
        }

        if (lower.contains("সাহায্য") || lower.contains("help") || lower.contains("কি করতে পারো") || lower.contains("capabilities")) {
            return OfflineResult(
                reply = "আমি কোডিং, আর্কিটেকচার ডিজাইন, বাগ ডিবাগিং, গণিত, অনুবাদ, ব্যক্তিগত মেমোরি এবং ভয়েস নির্দেশনায় সাহায্য করতে পারি। অ্যান্ড্রয়েড, কোটলিন, পাইথন, ডকার সহ বহু প্রযুক্তিতে পরামর্শ দিতে সক্ষম। নির্দেশ দিন।"
            )
        }

        // 4. Memory check
        if (lower.contains("মেমোরি") || lower.contains("মনে আছে") || lower.contains("what do you remember") || lower.contains("memory")) {
            if (contextMemories.isNotEmpty()) {
                val list = contextMemories.take(3).joinToString("; ")
                return OfflineResult(
                    reply = "আপনার সংরক্ষিত তথ্যের মধ্যে রয়েছে: $list। বিস্তারিত দেখতে হোম স্ক্রিনে Memory চাপুন।"
                )
            } else {
                return OfflineResult(
                    reply = "বর্তমানে কোনো ব্যক্তিগত মেমোরি সংরক্ষিত নেই। কিছু সংরক্ষণ করতে বলুন: 'এটা মনে রাখো ...'।"
                )
            }
        }

        // 5. Software Engineering, Architecture & Programming Knowledge
        if (lower.contains("compose") || lower.contains("jetpack") || lower.contains("android")) {
            return OfflineResult(
                reply = "Android Jetpack Compose একটি আধুনিক ডিক্লারেটিভ UI টুলকিট। এটি স্টেট ড্রিভেন আর্কিটেকচার অনুসরণ করে, যেখানে State পরিবর্তন হলে শুধুমাত্র প্রভাবিত Composable রি-কম্পোজ হয়। সাথে ViewModel, StateFlow এবং Clean Architecture ব্যবহার করে টেস্টেবল এবং স্কেলেবল অ্যাপ্লিকেশন তৈরি করা সর্বোত্তম অনুশীলন।"
            )
        }

        if (lower.contains("kotlin") || lower.contains("কোটলিন")) {
            return OfflineResult(
                reply = "Kotlin একটি আধুনিক স্ট্যাটিক্যালি টাইপড ভাষা যা নাল-সেফটি, এক্সটেনশন ফাংশন, সিল্ড ক্লাস এবং কোরুটিনস সাপোর্ট করে। কনকারেন্সির জন্য Coroutines ও লাইভ স্ট্রিমের জন্য Flow শিল্প মান হিসেবে সমাদৃত।"
            )
        }

        if (lower.contains("coroutine") || lower.contains("কোরুটিন")) {
            return OfflineResult(
                reply = "কোরুটিন হলো লাইটওয়েট থ্রেড। এটি ব্যাকগ্রাউন্ড কাজগুলো নন-ব্লকিং উপায়ে হ্যান্ডেল করে। Dispatchers.IO ব্যবহার করে ডাটাবেস ও নেটওয়ার্ক রিকোয়েস্ট এবং Dispatchers.Main ব্যবহার করে UI আপডেট করা হয়।"
            )
        }

        if (lower.contains("room") || lower.contains("database") || lower.contains("ডাটাবেস")) {
            return OfflineResult(
                reply = "Room হলো SQLite-এর উপর একটি অবস্ট্রাকশন লেয়ার। এতে Entity (টেবিল), DAO (কোয়েরি ইন্টারফেস) এবং AppDatabase থাকে। DAO মেথডে Flow রিটার্ন করলে ডাটাবেস পরিবর্তনের সাথে সাথে UI স্বয়ংক্রিয়ভাবে রিয়েক্টিভলি আপডেট হয়।"
            )
        }

        if (lower.contains("clean architecture") || lower.contains("mvvm") || lower.contains("architecture") || lower.contains("আর্কিটেকচার")) {
            return OfflineResult(
                reply = "Clean Architecture অ্যাপ্লিকেশনকে তিনটি প্রধান স্তরে ভাগ করে: Presentation (UI & ViewModel), Domain (UseCases & Business Rules), এবং Data (Repositories & DataSources)। ডিপেন্ডেন্সি সবসময় ভেতরের দিকে নির্দেশ করে, ফলে কোড মডুলার, ডিকাপল্ড এবং সহজে টেস্টেবল হয়।"
            )
        }

        if (lower.contains("docker") || lower.contains("ডকার")) {
            return OfflineResult(
                reply = "Docker কনটেইনারাইজেশন প্রযুক্তি প্রদান করে যা অ্যাপ্লিকেশন ও তার সব ডিপেন্ডেন্সিকে একটি আইসোলেটেড এনভায়রনমেন্টে বান্ডেল করে। এটি 'Build once, run anywhere' নীতি বাস্তবায়ন করে।"
            )
        }

        if (lower.contains("git") || lower.contains("গিট")) {
            return OfflineResult(
                reply = "Git একটি ডিস্ট্রিবিউটেড ভার্সন কন্ট্রোল সিস্টেম। ট্রাঙ্ক-বেসড ডেভেলপমেন্ট বা ফিচার ব্রাঞ্চিং স্ট্র্যাটেজি টিম কোলাবোরেশন ও সিআই/সিডি অটোমেশনের ভিত্তি।"
            )
        }

        if (lower.contains("python") || lower.contains("পাইথন")) {
            return OfflineResult(
                reply = "Python এআই, মেশিন লার্নিং ও ব্যাকএন্ড অটোমেশনের শীর্ষ ভাষা। PyTorch, TensorFlow এবং আধুনিক LLM ইকোসিস্টেমের মূল ড্রাইভার।"
            )
        }

        // 6. Simple Math Evaluation
        val mathMatch = Regex("""(\d+)\s*([\+\-\*\/])\s*(\d+)""").find(trimmed)
        if (mathMatch != null) {
            val (aStr, op, bStr) = mathMatch.destructured
            val a = aStr.toDoubleOrNull()
            val b = bStr.toDoubleOrNull()
            if (a != null && b != null) {
                val result = when (op) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> if (b != 0.0) a / b else null
                    else -> null
                }
                if (result != null) {
                    val formatted = if (result % 1.0 == 0.0) result.toLong().toString() else "%.2f".format(result)
                    return OfflineResult(reply = "গণনার ফলাফল: $a $op $b = $formatted")
                }
            }
        }

        // 7. Bengali/English Common Greetings
        if (lower == "হ্যালো" || lower == "hello" || lower == "hi" || lower == "হাই") {
            return OfflineResult(
                reply = "নমস্কার! আমি আপনার AI Assistant প্রস্তুত। আপনার নির্দেশ বলুন।"
            )
        }

        if (lower.contains("ধন্যবাদ") || lower.contains("thank you") || lower.contains("thanks")) {
            return OfflineResult(
                reply = "আপনাকে স্বাগতম! আমি সর্বদা আপনার স্পষ্ট নির্দেশে সাহায্য করতে প্রস্তুত।"
            )
        }

        // 8. General fallback with high-level reasoning
        return OfflineResult(
            reply = "আমি আপনার নির্দেশ বুঝতে পেরেছি: \"$trimmed\"। অফলাইন মোডে আমি বিশ্লেষণ সম্পন্ন করেছি। আপনার কোনো নির্দিষ্ট অ্যাকশন থাকলে নিশ্চিতকরণ সহ বলুন, অথবা অনলাইন মোড ব্যবহার করে আরও বিস্তারিত বিশ্লেষণ করতে পারেন।"
        )
    }
}
