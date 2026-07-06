package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.data.ScopeViewModel
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalsScreen(
    viewModel: ScopeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Form states
    var fullName by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("Corporate Gala") }
    var venueLocation by remember { mutableStateOf("") }
    var guestCountStr by remember { mutableStateOf("") }
    val selectedRequirements = remember { mutableStateListOf<String>() }
    var additionalDetails by remember { mutableStateOf("") }

    // Dropdown expanded state
    var dropdownExpanded by remember { mutableStateOf(false) }

    val showCustomRequestModal by viewModel.showCustomRequestModal.collectAsState()

    // Rentals Period Start/End Dates
    val startDate by viewModel.rentalsStartDate.collectAsState()
    val endDate by viewModel.rentalsEndDate.collectAsState()
    val periodDays = viewModel.rentalsPeriodDays

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- HERO OVERVIEW ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        "https://lh3.googleusercontent.com/aida-public/AB6AXuCgEI91T_oRgpd0H7EW4Cc-9cDgDBiUmLHgpznVSNDmFFL9u29f6YTKHzte3KdBmh_g7Kwh-i8IflmIZBLpMBc1zNgrjEv6oa25UgjH9L6QwaoNp72NxhyrpdAUXy5Zs9JD0BcooCMdg9GN5hHado4YTIvEljmpC7ESEvchC3WB5mzxDOmBApmE4z34sWo3MZAm4wA5M4DXgTXVwVt9AIQ8TDhAzk_WwYwn4fiPZOwmd6V-ye9WQ1_me-7OrOsYGhvSqJfhzTCq5i0Q"
                    ),
                    contentDescription = "Festival Speakers sunset",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.5f
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                             )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Elite Audio Solutions for Every Occasion",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "From weddings to corporate conferences, our premium sound equipment guarantees clinical acoustic precision.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // --- CHECK AVAILABILITY CALENDAR INPUT ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Check Availability",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select your event dates to view accurate booking rates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Date
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { viewModel.updateRentalsDates(it, endDate) },
                        label = { Text("Start Date (YYYY-MM-DD)", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_date_field"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.CalendarToday, "Start") }
                    )

                    // End Date
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { viewModel.updateRentalsDates(startDate, it) },
                        label = { Text("End Date (YYYY-MM-DD)", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("end_date_field"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, "End") }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected Period:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$periodDays Days",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Discounted weekly rates automatically applied for bookings over 5 days.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // --- CURATED PACKAGES ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Curated Sound Packages",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Engineered for performance, reliability, and ease of use.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            viewModel.rentalPackages.forEach { pkg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .testTag("package_${pkg.id}"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(pkg.imageUrl),
                                contentDescription = pkg.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            pkg.badge?.let { b ->
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SuccessGreen)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = b,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                Alignment.Top
                            ) {
                                Text(
                                    text = pkg.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${pkg.pricePerDay}/day",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "$${pkg.pricePerWeek}/week",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pkg.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Features bullet list
                            pkg.features.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Feature Checked",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = feature,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.addToCart(
                                        name = pkg.name,
                                        price = pkg.pricePerDay * periodDays,
                                        imageUrl = pkg.imageUrl,
                                        type = "Rental",
                                        subtitle = "${pkg.name} Rental for $periodDays Days"
                                    )
                                    Toast.makeText(
                                        context,
                                        "${pkg.name} sound booking added to cart!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add to Booking (${periodDays} days)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- BESPOKE SOLUTIONS DETAILS GRID ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "BESPOKE SOLUTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Need something truly unique?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Whether it's a multi-room setup, specific high-end microphone arrays, or full on-site technical support for a month-long tour, our specialists are ready to architect your custom audio ecosystem.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Request Custom Setup Form", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Description, "Form")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2x2 grid benefits
                data class RentalBenefit(
                    val icon: androidx.compose.ui.graphics.vector.ImageVector,
                    val title: String,
                    val description: String
                )

                val benefits = listOf(
                    RentalBenefit(Icons.Default.Build, "Technical Site Visit", "On-site acoustic analysis."),
                    RentalBenefit(Icons.Default.Person, "Certified Staff", "Engineers to manage live event."),
                    RentalBenefit(Icons.Default.SupportAgent, "24/7 Support", "Emergency replacement guaranteed."),
                    RentalBenefit(Icons.Default.Equalizer, "Top Brands", "Premium sound systems catalog.")
                )

                benefits.chunked(2).forEach { rowBenefits ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowBenefits.forEach { benefit ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(110.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Icon(
                                        imageVector = benefit.icon,
                                        contentDescription = benefit.title,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = benefit.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = benefit.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // --- SETUP QUESTIONNAIRE FORM ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Setup Questionnaire",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Tell us about your event, and we'll send a tailored quote within 4 hours.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("John Doe") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_fullname"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Event Type Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = eventType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Event Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true }
                            .testTag("form_eventtype"),
                        shape = RoundedCornerShape(24.dp),
                        trailingIcon = {
                            IconButton(onClick = { dropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Open Dropdown")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        listOf(
                            "Corporate Gala",
                            "Wedding Ceremony",
                            "Music Concert",
                            "Product Launch",
                            "Other"
                        ).forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    eventType = type
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Event Venue & Location
                OutlinedTextField(
                    value = venueLocation,
                    onValueChange = { venueLocation = it },
                    label = { Text("Event Venue & Location") },
                    placeholder = { Text("City Hall, Downtown District") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_location"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Guest Count
                OutlinedTextField(
                    value = guestCountStr,
                    onValueChange = { guestCountStr = it },
                    label = { Text("Guest Count") },
                    placeholder = { Text("e.g. 150") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_guestcount"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Requirements Checkboxes
                Text(
                    text = "Key Requirements",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val requirementsList = listOf(
                    "Stage Lighting",
                    "Live Recording",
                    "Engineer On-site",
                    "Wireless Mics"
                )

                requirementsList.forEach { req ->
                    val isChecked = selectedRequirements.contains(req)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedRequirements.remove(req)
                                else selectedRequirements.add(req)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked == true) selectedRequirements.add(req)
                                else selectedRequirements.remove(req)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(text = req, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Additional Details
                OutlinedTextField(
                    value = additionalDetails,
                    onValueChange = { additionalDetails = it },
                    label = { Text("Additional Details") },
                    placeholder = { Text("Describe space dimensions, acoustic challenges, schedule...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("form_details"),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.submitCustomSetup(
                            fullName = fullName,
                            eventType = eventType,
                            location = venueLocation,
                            guestCountStr = guestCountStr,
                            requirements = selectedRequirements.toList(),
                            details = additionalDetails
                        )
                        // Clear form
                        fullName = ""
                        venueLocation = ""
                        guestCountStr = ""
                        selectedRequirements.clear()
                        additionalDetails = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_rental_form"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Submit Request", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Spacer for bottom navigation
        Spacer(modifier = Modifier.height(88.dp))
    }

    // --- SUCCESS MODAL ---
    if (showCustomRequestModal) {
        Dialog(onDismissRequest = { viewModel.setCustomRequestModalVisible(false) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = SuccessGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Text(
                        text = "Request Received",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "One of our audio specialists will reach out to you shortly to finalize your custom setup.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Button(
                        onClick = { viewModel.setCustomRequestModalVisible(false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("close_success_dialog"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Back to Rentals", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
