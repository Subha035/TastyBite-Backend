package com.example.backend.service;

import com.example.backend.model.ActivityLog;
import com.example.backend.model.MenuItem;
import com.example.backend.model.Offer;
import com.example.backend.model.Order;
import com.example.backend.model.Reservation;
import com.example.backend.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class FirebaseService {

    @Autowired(required = false)
    private Firestore firestore;

    // Fast in-memory cache to avoid remote network latency on every read
    private final List<MenuItem> cachedMenuItems = new CopyOnWriteArrayList<>();
    private volatile long lastMenuCacheTime = 0;

    private final List<Offer> cachedOffers = new CopyOnWriteArrayList<>();
    private volatile long lastOffersCacheTime = 0;

    private static final long CACHE_TTL_MS = 60_000; // 60 seconds cache

    // In-memory fallbacks when Firestore credentials are not configured locally
    private final Map<String, MenuItem> memoryMenuItems = new ConcurrentHashMap<>();
    private final Map<String, Reservation> memoryReservations = new ConcurrentHashMap<>();
    private final Map<String, Offer> memoryOffers = new ConcurrentHashMap<>();
    private final Map<String, Order> memoryOrders = new ConcurrentHashMap<>();
    private final Map<String, User> memoryUsers = new ConcurrentHashMap<>();
    private final Map<String, ActivityLog> memoryActivities = new ConcurrentHashMap<>();

    public FirebaseService() {
        seedInitialData();
    }

    @PostConstruct
    public void preWarmCache() {
        // Pre-warm cache in background on startup so first user request is instant
        CompletableFuture.runAsync(() -> {
            try {
                getAllMenuItems();
                getAllOffers();
            } catch (Exception ignored) {}
        });
    }

    private void seedInitialData() {
        // Initial Menu Seed
        List<MenuItem> initialMenu = List.of(
            new MenuItem("1", "Veg Supreme Pizza", "Capsicum, Onion, Tomato, Corn, Black Olives, Mushroom, Cheese", 299.0, "Pizza", true, "/veg_supreme_pizza.png", 4.8, true),
            new MenuItem("2", "Margherita Pizza", "Classic delight with 100% real mozzarella cheese & basil.", 249.0, "Pizza", true, "/margherita_pizza.png", 4.6, true),
            new MenuItem("3", "Paneer Tikka Pizza", "Flavorful paneer tikka, capsicum, onion with cheesy topping.", 319.0, "Pizza", true, "/paneer_tikka_pizza.png", 4.9, true),
            new MenuItem("4", "Chicken Pepperoni Pizza", "Loaded with spicy chicken pepperoni & extra cheese.", 349.0, "Pizza", false, "/margherita_pizza.png", 4.7, true),
            new MenuItem("5", "Stuffed Garlic Bread", "Freshly baked garlic bread filled with mozzarella & corn.", 149.0, "Starters", true, "/veg_supreme_pizza.png", 4.5, true),
            new MenuItem("6", "Chocolate Brownie Fudge", "Warm chocolate brownie served with vanilla ice cream.", 179.0, "Desserts", true, "/paneer_tikka_pizza.png", 4.9, true)
        );
        for (MenuItem item : initialMenu) {
            memoryMenuItems.put(item.getId(), item);
        }

        // Initial Offers Seed
        List<Offer> initialOffers = List.of(
            new Offer("1", "Super Saver Flat 20% OFF", "SAVE20", 20.0, 100.0, 499.0, "Get flat 20% discount on all orders above ₹499", "2026-12-31", true),
            new Offer("2", "Free Garlic Bread Weekend", "FREEGB", 100.0, 149.0, 699.0, "Complimentary Stuffed Garlic Bread on orders above ₹699", "2026-12-31", true)
        );
        for (Offer offer : initialOffers) {
            memoryOffers.put(offer.getId(), offer);
        }

        // Initial Activities Seed
        List<ActivityLog> initialActivities = List.of(
            new ActivityLog("act_1", "Table reservation for 4 guests confirmed", "reservation", "3m ago", System.currentTimeMillis() - 180000),
            new ActivityLog("act_2", "Offer campaign 'SAVE20' updated", "coupon", "18m ago", System.currentTimeMillis() - 1080000),
            new ActivityLog("act_3", "Item 'Chicken Cheese Burger' marked Out of Stock", "stock", "35m ago", System.currentTimeMillis() - 2100000),
            new ActivityLog("act_4", "New menu item 'Veg Supreme Pizza' updated", "menu", "1h ago", System.currentTimeMillis() - 3600000)
        );
        for (ActivityLog act : initialActivities) {
            memoryActivities.put(act.getId(), act);
        }
    }

    // --- MENU ITEMS ---
    public List<MenuItem> getAllMenuItems() {
        if (!cachedMenuItems.isEmpty() && (System.currentTimeMillis() - lastMenuCacheTime < CACHE_TTL_MS)) {
            return new ArrayList<>(cachedMenuItems);
        }

        if (firestore != null) {
            try {
                ApiFuture<QuerySnapshot> future = firestore.collection("menuItems").get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                if (!docs.isEmpty()) {
                    List<MenuItem> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : docs) {
                        MenuItem item = doc.toObject(MenuItem.class);
                        item.setId(doc.getId());
                        items.add(item);
                    }
                    cachedMenuItems.clear();
                    cachedMenuItems.addAll(items);
                    lastMenuCacheTime = System.currentTimeMillis();
                    return items;
                }
            } catch (Exception e) {
                System.err.println("Firestore menu read error: " + e.getMessage());
            }
        }
        return new ArrayList<>(memoryMenuItems.values());
    }

    public MenuItem saveMenuItem(MenuItem item) {
        if (item.getId() == null || item.getId().isEmpty()) {
            item.setId(UUID.randomUUID().toString());
        }
        if (firestore != null) {
            try {
                firestore.collection("menuItems").document(item.getId()).set(item);
            } catch (Exception e) {
                System.err.println("Firestore menu write error: " + e.getMessage());
            }
        }
        memoryMenuItems.put(item.getId(), item);
        cachedMenuItems.removeIf(m -> m.getId() != null && m.getId().equals(item.getId()));
        cachedMenuItems.add(item);
        lastMenuCacheTime = System.currentTimeMillis();
        return item;
    }

    public boolean deleteMenuItem(String id) {
        if (firestore != null) {
            try {
                firestore.collection("menuItems").document(id).delete();
            } catch (Exception e) {
                System.err.println("Firestore menu delete error: " + e.getMessage());
            }
        }
        cachedMenuItems.removeIf(m -> m.getId() != null && m.getId().equals(id));
        lastMenuCacheTime = System.currentTimeMillis();
        return memoryMenuItems.remove(id) != null;
    }

    // --- RESERVATIONS ---
    public List<Reservation> getAllReservations() {
        if (firestore != null) {
            try {
                ApiFuture<QuerySnapshot> future = firestore.collection("reservations").get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                List<Reservation> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : docs) {
                    Reservation res = doc.toObject(Reservation.class);
                    res.setId(doc.getId());
                    list.add(res);
                }
                return list;
            } catch (Exception e) {
                System.err.println("Firestore reservation read error: " + e.getMessage());
            }
        }
        List<Reservation> resList = new ArrayList<>(memoryReservations.values());
        resList.sort((a, b) -> Objects.toString(b.getCreatedAt(), "").compareTo(Objects.toString(a.getCreatedAt(), "")));
        return resList;
    }

    public Reservation createReservation(Reservation reservation) {
        if (reservation.getId() == null || reservation.getId().isEmpty()) {
            reservation.setId(UUID.randomUUID().toString());
        }
        if (reservation.getCreatedAt() == null) {
            reservation.setCreatedAt(LocalDateTime.now().toString());
        }
        if (reservation.getStatus() == null) {
            reservation.setStatus("PENDING");
        }
        if (firestore != null) {
            try {
                firestore.collection("reservations").document(reservation.getId()).set(reservation);
            } catch (Exception e) {
                System.err.println("Firestore reservation write error: " + e.getMessage());
            }
        }
        memoryReservations.put(reservation.getId(), reservation);
        return reservation;
    }

    public Reservation updateReservationStatus(String id, String status) {
        Reservation res = memoryReservations.get(id);
        if (res != null) {
            res.setStatus(status);
        }
        if (firestore != null) {
            try {
                DocumentReference docRef = firestore.collection("reservations").document(id);
                docRef.update("status", status);
            } catch (Exception e) {
                System.err.println("Firestore reservation update error: " + e.getMessage());
            }
        }
        return res;
    }

    public boolean deleteReservation(String id) {
        if (firestore != null) {
            try {
                firestore.collection("reservations").document(id).delete();
            } catch (Exception e) {
                System.err.println("Firestore reservation delete error: " + e.getMessage());
            }
        }
        return memoryReservations.remove(id) != null;
    }

    // --- OFFERS ---
    public List<Offer> getAllOffers() {
        if (!cachedOffers.isEmpty() && (System.currentTimeMillis() - lastOffersCacheTime < CACHE_TTL_MS)) {
            return new ArrayList<>(cachedOffers);
        }

        if (firestore != null) {
            try {
                ApiFuture<QuerySnapshot> future = firestore.collection("offers").get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                if (!docs.isEmpty()) {
                    List<Offer> offers = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : docs) {
                        Offer offer = doc.toObject(Offer.class);
                        offer.setId(doc.getId());
                        offers.add(offer);
                    }
                    cachedOffers.clear();
                    cachedOffers.addAll(offers);
                    lastOffersCacheTime = System.currentTimeMillis();
                    return offers;
                }
            } catch (Exception e) {
                System.err.println("Firestore offer read error: " + e.getMessage());
            }
        }
        return new ArrayList<>(memoryOffers.values());
    }

    public Offer saveOffer(Offer offer) {
        if (offer.getId() == null || offer.getId().isEmpty()) {
            offer.setId(UUID.randomUUID().toString());
        }
        if (firestore != null) {
            try {
                firestore.collection("offers").document(offer.getId()).set(offer);
            } catch (Exception e) {
                System.err.println("Firestore offer write error: " + e.getMessage());
            }
        }
        memoryOffers.put(offer.getId(), offer);
        cachedOffers.removeIf(o -> o.getId() != null && o.getId().equals(offer.getId()));
        cachedOffers.add(offer);
        lastOffersCacheTime = System.currentTimeMillis();
        return offer;
    }

    public boolean deleteOffer(String id) {
        if (firestore != null) {
            try {
                firestore.collection("offers").document(id).delete();
            } catch (Exception e) {
                System.err.println("Firestore offer delete error: " + e.getMessage());
            }
        }
        cachedOffers.removeIf(o -> o.getId() != null && o.getId().equals(id));
        lastOffersCacheTime = System.currentTimeMillis();
        return memoryOffers.remove(id) != null;
    }

    // --- ORDERS ---
    public List<Order> getAllOrders() {
        if (firestore != null) {
            try {
                ApiFuture<QuerySnapshot> future = firestore.collection("orders").get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                List<Order> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : docs) {
                    Order order = doc.toObject(Order.class);
                    order.setId(doc.getId());
                    memoryOrders.put(order.getId(), order);
                    list.add(order);
                }
                return list;
            } catch (Exception e) {
                System.err.println("Firestore order read error: " + e.getMessage());
            }
        }
        return new ArrayList<>(memoryOrders.values());
    }

    public Order createOrder(Order order) {
        if (order.getId() == null || order.getId().isEmpty()) {
            order.setId(UUID.randomUUID().toString());
        }
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            order.setOrderId("TB-" + (1000 + (int)(Math.random() * 9000)));
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now().toString());
        }
        if (order.getStatus() == null) {
            order.setStatus("PREPARING");
        }
        if (firestore != null) {
            try {
                firestore.collection("orders").document(order.getId()).set(order);
            } catch (Exception e) {
                System.err.println("Firestore order write error: " + e.getMessage());
            }
        }
        memoryOrders.put(order.getId(), order);
        return order;
    }

    public Order updateOrderStatus(String id, String status) {
        Order order = memoryOrders.get(id);
        if (order != null) {
            order.setStatus(status);
        }
        if (firestore != null) {
            try {
                firestore.collection("orders").document(id).update("status", status);
                if (order == null) {
                    DocumentSnapshot doc = firestore.collection("orders").document(id).get().get();
                    if (doc.exists()) {
                        order = doc.toObject(Order.class);
                        if (order != null) {
                            order.setId(doc.getId());
                            order.setStatus(status);
                            memoryOrders.put(id, order);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Firestore order status update error: " + e.getMessage());
            }
        }
        return order;
    }

    public Order updateOrderPaymentStatus(String id, String paymentStatus) {
        Order order = memoryOrders.get(id);
        if (order != null) {
            order.setPaymentStatus(paymentStatus);
        }
        if (firestore != null) {
            try {
                firestore.collection("orders").document(id).update("paymentStatus", paymentStatus);
                if (order == null) {
                    DocumentSnapshot doc = firestore.collection("orders").document(id).get().get();
                    if (doc.exists()) {
                        order = doc.toObject(Order.class);
                        if (order != null) {
                            order.setId(doc.getId());
                            order.setPaymentStatus(paymentStatus);
                            memoryOrders.put(id, order);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Firestore order payment status update error: " + e.getMessage());
            }
        }
        return order;
    }

    // --- USERS ---
    public User getUserByEmail(String email) {
        if (email == null) return null;
        if (firestore != null) {
            try {
                ApiFuture<QuerySnapshot> future = firestore.collection("users").whereEqualTo("email", email.toLowerCase()).get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                if (!docs.isEmpty()) {
                    User user = docs.get(0).toObject(User.class);
                    user.setId(docs.get(0).getId());
                    return user;
                }
            } catch (Exception e) {
                System.err.println("Firestore user read error: " + e.getMessage());
            }
        }
        return memoryUsers.values().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst()
                .orElse(null);
    }

    public User getUserById(String id) {
        if (id == null) return null;
        if (firestore != null) {
            try {
                DocumentSnapshot doc = firestore.collection("users").document(id).get().get();
                if (doc.exists()) {
                    User user = doc.toObject(User.class);
                    user.setId(doc.getId());
                    return user;
                }
            } catch (Exception e) {
                System.err.println("Firestore user read by id error: " + e.getMessage());
            }
        }
        return memoryUsers.get(id);
    }

    public User saveUser(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        }
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().toLowerCase());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now().toString());
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getAuthProvider() == null) {
            user.setAuthProvider("local");
        }
        if (firestore != null) {
            try {
                firestore.collection("users").document(user.getId()).set(user);
            } catch (Exception e) {
                System.err.println("Firestore user write error: " + e.getMessage());
            }
        }
        memoryUsers.put(user.getId(), user);
        return user;
    }

    // --- ACTIVITIES ---
    public List<ActivityLog> getAllActivities() {
        if (firestore != null) {
            try {
                ApiFuture<QuerySnapshot> future = firestore.collection("activities").get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                if (!docs.isEmpty()) {
                    List<ActivityLog> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : docs) {
                        ActivityLog log = doc.toObject(ActivityLog.class);
                        log.setId(doc.getId());
                        logs.add(log);
                    }
                    logs.sort((a, b) -> Long.compare(b.getTimestamp() != null ? b.getTimestamp() : 0, a.getTimestamp() != null ? a.getTimestamp() : 0));
                    return logs;
                }
            } catch (Exception e) {
                System.err.println("Firestore activity read error: " + e.getMessage());
            }
        }
        List<ActivityLog> list = new ArrayList<>(memoryActivities.values());
        list.sort((a, b) -> Long.compare(b.getTimestamp() != null ? b.getTimestamp() : 0, a.getTimestamp() != null ? a.getTimestamp() : 0));
        return list;
    }

    public ActivityLog logActivity(String title, String category) {
        if (title == null || title.trim().isEmpty()) return null;
        ActivityLog act = new ActivityLog();
        act.setId(UUID.randomUUID().toString());
        act.setTitle(title.trim());
        act.setCategory(category != null ? category : "system");
        act.setTime("Just now");
        act.setTimestamp(System.currentTimeMillis());

        if (firestore != null) {
            try {
                firestore.collection("activities").document(act.getId()).set(act);
            } catch (Exception e) {
                System.err.println("Firestore activity write error: " + e.getMessage());
            }
        }
        memoryActivities.put(act.getId(), act);
        return act;
    }
}
