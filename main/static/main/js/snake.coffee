
commentTemplate =
    '<div class="comment-box" style="display: none">' +
    '    <div class="comment-internals">' +
    '        <div>' +
    '            <p class="comment-author">author</p>' +
    '            <p class="comment-body">body</p>' +
    '        </div>' +
    '        <div>' +
    '            <p style="text-align: right">' +
    '                <a href="javascript:void(0)" class="reply">Reply</a>' +
    '            </p>' +
    '        </div>' +
    '    </div>' +
    '</div>'


commentFormTemplate =
    '<div class="new-comment">' +
    '    <div class="comment-form">' +
    '        <p class="alert alert-warning" style="display: none">Something\'s gone wrong, Jim. Please try again in a bit.</p>' +
    '        <textarea placeholder="Leave a comment" class="comment-field"></textarea>' +
    '        <div class="comment-slider" style="display: none">' +
    '            <button class="save-button form-item">Save</button>' +
    '            <input type="text" placeholder="Name" class="name-field form-item">' +
    '        </div>' +
    '    </div>' +
    '</div>'



empty = (x) ->
  x.val().length == 0

rumble = (element, amount) ->
  return if amount == 0
  element.css('position', 'relative')
  element.animate({'left': '1px'}, 50)
         .animate({'left': '0px'}, 50)
  rumble(element, amount - 1)


displayError = (element) ->
  element.css('border-color', '#F44336')
  rumble(element, 4)

buildComment = (data) ->
  commentBox = $(commentTemplate)
  commentBox.attr('id', data.comment_id)
  commentBox.find('p.comment-author').text(data.name)
  commentBox.find('p.comment-body').text(data.body)
  commentBox.find('a.reply').hide()
  commentBox

markAsNewComment = ($newComment) ->
  1234

insertComment = (element, data) ->
  parent = element.parents('.comment-box:first')
  commentBox = buildComment(data)

  if (parent.length != 0)
    parent.find('.comment-internals:first').after(commentBox.prop('outerHTML'))
  else
    $('.root-comment').prepend(commentBox.prop('outerHTML'))

  newComment = $('#' + data.comment_id)
  newComment.next().css('margin-left': 0)
  newComment.fadeIn()
            .find('.comment-author')
            .css('color', '#9f5512')


updateCommentCount = () ->
  $commentNumber = $('#total-comments')
  currentParsedNumber = parseInt($commentNumber.text())
  $commentNumber.text(currentParsedNumber + 1)



saveComment = (element) ->
  parent = element.parents('.comment-box')
  if (parent.length == 0)
    parentId = null # $('.root-comment .comment-box').attr('id')
  else
    parentId = parent.attr('id')

  new Promise((resolve, reject) ->
    $.ajax('./storecomment/', {
        type: 'POST'
        data: {
          'parent_id': parentId,
          'body': element.parent().prev('textarea').val(),
          'name': element.next('.name-field').val()
        },
        xhrFields: {
          withCredentials: true
        },
    })
    .done((data) ->
      console.log("success!")
      resolve(data))
    .fail((err) -> reject(err))
  )

getCsrfCookie = () ->
  ($.trim(cookie.split('=')[1]) for cookie in document.cookie.split(';') when cookie.indexOf('csrftoken') > -1)

resetInputBorderColorOnClick = (defaultBorderColor) ->
  $(document).on('click', '.comment-field, .name-field', (evt) ->
    $(this).css('border-color', defaultBorderColor)
  )


jQuery ($) ->

  commentBox = $('.comment-field')
  defaultBorderColor = commentBox.css('border-color')
  resetInputBorderColorOnClick(defaultBorderColor)

  commentBox.on('click', () ->
    $(this).animate({'height': '120px'}, 'slow')
           .unbind()
           .next('.comment-slider').slideDown()
  )


  $(document).on('click', '.save-button', (evt) ->
    evt.preventDefault()
    comment = $(this).parents(".new-comment").find('textarea')
    name = $(this).next('.name-field')

    if (empty(comment))
      displayError(comment)
    if (empty(name))
      displayError(name)
    if (not empty(comment) and not empty(name))
      saveComment($(this)).then(
        ((res) ->
          insertComment($(evt.target), res)
          updateCommentCount()
          setTimeout((()->
            $(evt.target).parents(".new-comment").slideUp()), 500)
        )
      , (() ->
          error = $(evt.target).parents('div.comment-form').find('p.alert')
          error.css('display', 'block')
          rumble(error, 2)
        )
      )
  )


  $('.comment-box .reply').on('click', () ->
    parent = $(this).parents('.comment-internals')
    parentAuthor = parent.find('p.comment-author')
    child = $(commentFormTemplate)
    child.appendTo(parent)
    child.find('.comment-field, .name-field').css('border-color', defaultBorderColor)
    child.find('textarea').attr('placeholder', 'replying to ' + parentAuthor.text()).animate({'height': '120px'}, 'slow')
    child.find('.comment-slider').slideDown()
    $(this).hide()
  )
