package org.birdview.source

class ItemsIterable<Item, Continuation>(
        val firstItemsPage:ItemsPage<Item, Continuation> = ItemsPage(),
        val loadNextPageLoader: ((Continuation) -> ItemsPage<Item, Continuation>?)? = null
) : Iterable<Item> {
    override fun iterator(): Iterator<Item> {
        return object : Iterator<Item> {
            private var nextContinuation = firstItemsPage.continuation
            private var itemsIterator = firstItemsPage.items.iterator()
            override fun hasNext(): Boolean {
                if(!itemsIterator.hasNext()) {
                    loadNextPage()
                }
                return itemsIterator.hasNext()
            }

            override fun next(): Item =
                if(hasNext()) {
                    itemsIterator.next()
                } else {
                    throw NoSuchElementException()
                }

            private fun loadNextPage() {
                val currentContinuation = nextContinuation
                if (currentContinuation != null && loadNextPageLoader != null) {
                    loadNextPageLoader.invoke(currentContinuation)?.let { page ->
                        itemsIterator = page.items.iterator()
                        nextContinuation = page.continuation
                    }
                }
            }
        }
    }
}

class ItemsPage<Item, Continuation> (
        val items:List<Item> = listOf(),
        val continuation: Continuation? = null
)