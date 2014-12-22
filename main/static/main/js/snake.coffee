
commentTemplate = '<div class="comment-box template" id="{{ comment.id }}" style="display: none;">' +
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


insertComment = (element, data) ->
  parent = element.parents('.comment-box')
  commentBox = $($.parseHTML(commentTemplate))
  commentBox.attr('id', data.comment_id)
  commentBox.find('p.comment-author').text(data.name)
  commentBox.find('p.comment-body').text(data.body)
  commentBox.find('a.reply').hide()

  if (parent.length != 0)
    parent.children('.comment-box:first').prepend(commentBox.html())
  else
    console.log("asdfasdf")
    $('.root-comment').children('.comment-box:first').prepend(commentBox.html())


saveComment = (element) ->
  parent = element.parents('.comment-box')
  if (parent.length == 0)
    parentId = $('.root-comment .comment-box').attr('id')
  else
    parentId = parent.attr('id')

  console.log('NAME: ' + element.next('.name-field').val())
  new Promise((resolve, reject) ->
    $.ajax('./storecomment/', {
        type: 'POST'
        data: {
          'parent_id': parseInt(parentId),
          'body': element.parent().prev('textarea').val(),
          'name': element.next('.name-field').val()
        },
        xhrFields: {
          withCredentials: true
        },
    })
    .done((data) ->
      console.log("sucess!")
      resolve(data))
    .fail((err) -> reject(err))
  )

getCsrfCookie = () ->
  ($.trim(cookie.split('=')[1]) for cookie in document.cookie.split(';') when cookie.indexOf('csrftoken') > -1)


jQuery ($) ->

  commentBox = $('.comment-field')
  newComment = $('.new-comment')

  defaultBorderColor = commentBox.css('border-color')
  console.log(defaultBorderColor)
  commentBox.on('click', () ->
    $(this).animate({'height': '120px'}, 'slow')
           .unbind()
           .next('.comment-slider').slideDown()
  )

  $(document).on('click', '.comment-field, .name-field', (evt) ->
    $(this).css('border-color', defaultBorderColor)
  )

  $(document).on('click', '.save-button', (evt) ->
    evt.preventDefault()
    comment = $(this).parent().prev('textarea')
    name = $(this).next('.name-field')

    console.log(comment.val().length == 0)
    console.log(name.val().length == 0)


    if (empty(comment))
      displayError(comment)
    if (empty(name))
      displayError(name)
    if (not empty(comment) and not empty(name))
      saveComment($(this)).then(
        ((res) ->
          $(evt.target).parents(".new-comment").slideUp()
          insertComment($(evt.target), res)
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
    child = newComment.clone()
    child.appendTo(parent)
    child.find('.comment-field, .name-field').css('border-color', defaultBorderColor)
    child.find('textarea').attr('placeholder', 'replying to ' + parentAuthor.text()).animate({'height': '120px'}, 'slow')
    child.find('.comment-slider').slideDown()
    $(this).hide()
  )
