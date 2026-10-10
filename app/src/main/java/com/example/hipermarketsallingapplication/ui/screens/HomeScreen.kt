package com.example.hipermarketsallingapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hipermarketsallingapplication.AppDestination
import com.example.hipermarketsallingapplication.data.model.Product
import com.example.hipermarketsallingapplication.ui.theme.*
import com.example.hipermarketsallingapplication.ui.viewmodel.PosViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posViewModel: PosViewModel,
    onOpenActiveCart: () -> Unit,
    onNavigateTo: (AppDestination) -> Unit
) {
    val products by posViewModel.allHomeProducts.collectAsState()

    LaunchedEffect(Unit) {
        posViewModel.loadHomeProducts()
    }

    var localHomeSearch by remember { mutableStateOf("") }
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }

    // When searching, bring matching product posts to the top of the feed!
    val sortedProducts = remember(products, localHomeSearch) {
        if (localHomeSearch.isBlank()) products else {
            val (matched, others) = products.partition {
                it.name.contains(localHomeSearch, ignoreCase = true) ||
                        it.code.contains(localHomeSearch, ignoreCase = true) ||
                        it.type.contains(localHomeSearch, ignoreCase = true)
            }
            matched + others
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        val screenWidthDp = maxWidth
        // Adapt column count based on screen width (1 phone size width = 1 column, 2 phone sizes = 2 columns, etc.)
        val columnCount = maxOf(1, (screenWidthDp / 360.dp).toInt())

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Fixed Search Bar & Active Cart Button on Home
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = localHomeSearch,
                    onValueChange = { localHomeSearch = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    label = { Text("Search catalog & store products...", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = DarkBlueAccent) },
                    trailingIcon = {
                        if (localHomeSearch.isNotEmpty()) {
                            IconButton(onClick = { localHomeSearch = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlueAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                // Active Cart button on the right side of search bar (opens popup panel)
                Button(
                    onClick = onOpenActiveCart,
                    modifier = Modifier.height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Active Cart", tint = TextLight)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (columnCount == 1) {
                // 1 product post per column for standard phone size
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sortedProducts) { product ->
                        InstagramProductPostCard(
                            product = product,
                            onAddToCart = {
                                posViewModel.addToCart(product, 1)
                            },
                            onInspect = {
                                selectedProductForDetail = product
                            }
                        )
                    }
                }
            } else {
                // 2 or more columns for wider screens (tablets / foldables / multi-phone size width)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sortedProducts) { product ->
                        InstagramProductPostCard(
                            product = product,
                            onAddToCart = {
                                posViewModel.addToCart(product, 1)
                            },
                            onInspect = {
                                selectedProductForDetail = product
                            }
                        )
                    }
                }
            }
        }
    }

    selectedProductForDetail?.let { product ->
        ProductDetailModal(
            product = product,
            onDismiss = { selectedProductForDetail = null },
            onAddToCartWithSpecs = { qty, size, color ->
                posViewModel.addToCart(product, qty)
            }
        )
    }
}

@Composable
fun InstagramProductPostCard(
    product: Product,
    onAddToCart: () -> Unit,
    onInspect: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }
    var isCommentsExpanded by remember { mutableStateOf(false) } // Hidden by default
    var newCommentText by remember { mutableStateOf("") }
    val comments = remember { mutableStateListOf("Super high quality product!", "Restocked yesterday.", "Fast seller at Terminal 1.") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspect() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Instagram Header: Shop Name, Brand Name & Follow Shop Button before '...' Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(DarkBlueAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.atShop.take(1).uppercase(),
                            color = TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = product.type, // Brand
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = product.atShop, // Shop Name
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Verified Store",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Right side: Follow Shop button before '...' menu button
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    var isFollowing by remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = { isFollowing = !isFollowing },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isFollowing) DarkBlueAccent else Color.Transparent,
                            contentColor = if (isFollowing) TextLight else DarkBlueAccent
                        ),
                        border = BorderStroke(1.dp, DarkBlueAccent)
                    ) {
                        Text(if (isFollowing) "Following" else "Follow", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextMuted)
                    }
                }
            }

            // Big Product Photo Box (Instagram Image Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = product.name,
                        tint = DarkBlueAccent,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Code: ${product.code} • Barcode: ${product.barcode}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Big Price Tag Overlay
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = EnergyRed),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.2f", product.price)}",
                        color = TextLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Stock Badge Overlay
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (product.quantity < 10) EnergyRed else SuccessGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Stock: ${product.quantity} units",
                        color = TextLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Instagram Interactive Action Bar (Like, Share, Add To Cart, Bookmark)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isLiked = !isLiked }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) EnergyRed else TextLight
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = TextLight)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add To Cart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { isBookmarked = !isBookmarked }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isBookmarked) DarkBlueAccent else TextLight
                        )
                    }
                }
            }

            // Post Description, Likes Count & Comments Toggle Label
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text(
                    text = if (isLiked) "Liked by you and 24 staff members" else "24 likes",
                    color = TextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "hipermarket_store Premium catalog item: ${product.name}. High demand item available at Main Terminal.",
                    color = TextLight,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isCommentsExpanded) "Hide comments" else "View all ${comments.size} comments...",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { isCommentsExpanded = !isCommentsExpanded }
                )
            }

            // Inline Comments List (ONLY listed when user presses View all comments label)
            if (isCommentsExpanded) {
                HorizontalDivider(color = DarkBlueDarker, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    comments.forEach { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("staff_user: ", color = DarkBlueAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(comment, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        }
                    }

                    // Inline Comment Input Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Add a comment...", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                    if (newCommentText.isNotBlank()) {
                                    comments.add(newCommentText)
                                    newCommentText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlueAccent),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Post", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailModal(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCartWithSpecs: (qty: Int, size: String, color: String) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var selectedSize by remember { mutableStateOf("M") }
    var selectedColor by remember { mutableStateOf("Black") }

    val sizes = listOf("S", "M", "L", "XL", "XXL")
    val colors = listOf("Black", "White", "Blue", "Red", "Grey")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = DarkBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Product Specifications", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Full Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(product.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Divider(color = DarkBlueDarker, thickness = 0.5.dp)
                        Text("🏷️ Brand / Type: ${product.type}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                        Text("🏬 Store / Shop: ${product.atShop}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                        Text("📦 Product Code: ${product.code}", color = TextMuted, fontSize = 12.sp)
                        Text("📊 Barcode: ${product.barcode}", color = TextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Price: $${String.format(Locale.getDefault(), "%.2f", product.price)}", color = EnergyRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Cost: $${String.format(Locale.getDefault(), "%.2f", product.cost)}", color = TextMuted, fontSize = 13.sp)
                        }
                        Text("Tax Rate: ${product.tax}% | Include Tax: ${product.includeTax}", color = TextMuted, fontSize = 12.sp)
                        Text("Stock Available: ${product.quantity} units", color = if (product.quantity < 10) EnergyRed else SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (product.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Description:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            Text(product.description, color = TextMuted, fontSize = 12.sp)
                        }
                        if (product.moreInfo.isNotBlank()) {
                            Text("More Info: ${product.moreInfo}", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }

                Text("Select Size:", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    sizes.forEach { size ->
                        val isSelected = selectedSize == size
                        Button(
                            onClick = { selectedSize = size },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) DarkBlueAccent else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) TextLight else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(size, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Text("Select Color:", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    colors.forEach { color ->
                        val isSelected = selectedColor == color
                        Button(
                            onClick = { selectedColor = color },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) DarkBlueAccent else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) TextLight else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(color, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Text("Quantity:", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    Text(
                        text = quantity.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    IconButton(
                        onClick = { if (quantity < product.quantity) quantity++ },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddToCartWithSpecs(quantity, selectedSize, selectedColor)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add $quantity To Cart ($selectedSize, $selectedColor)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    )
}
